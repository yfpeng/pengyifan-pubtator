package com.pengyifan.pubtator.utils;

import com.google.common.collect.Sets;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public class PubTatorEval {
  private final List<PubTatorDocument> goldDocuments;
  private final List<PubTatorDocument> predDocuments;

  private PrecisionRecallStats diseaseMentionStats;
  private PrecisionRecallStats chemicalMentionStats;
  private PrecisionRecallStats cdrStats;
  private PrecisionRecallStats diseaseIdStats;
  private PrecisionRecallStats chemicalIdStats;

  public PubTatorEval(List<PubTatorDocument> gold, List<PubTatorDocument> pred) {
    this.goldDocuments = gold;
    this.predDocuments = pred;
  }

  public String getResult() {
    ResultPrinter resultPrinter = new ResultPrinter();
    resultPrinter.printTitle();
    resultPrinter.printRow("Disease  (Mention)", diseaseMentionStats);
    resultPrinter.printRow("Disease  (ID)", diseaseIdStats);
    resultPrinter.printRow("Chemical (Mention)", chemicalMentionStats);
    resultPrinter.printRow("Chemical (ID)", chemicalIdStats);
    resultPrinter.printRow("CDR");
    resultPrinter.printRow("  CID", cdrStats);
    resultPrinter.printRow("  Disease  (ID)", diseaseIdStats);
    resultPrinter.printRow("  Chemical (ID)", chemicalIdStats);
    return resultPrinter.toString();
  }

  public void eval() {
    evalMention();
    evalCdr();
    evalId();
  }

  private void evalId() {
    Collection<String> goldIds = uniqueIds(getAllMentions(goldDocuments, "Disease"));
    Collection<String> predIds = uniqueIds(getAllMentions(predDocuments, "Disease"));
    final BiPredicate<String, String> biPredicate =
        (m1, m2) -> m1.equals(m2);
    diseaseIdStats = eval(goldIds, predIds, biPredicate);

    goldIds = uniqueIds(getAllMentions(goldDocuments, "Chemical"));
    predIds = uniqueIds(getAllMentions(predDocuments, "Chemical"));
    chemicalIdStats = eval(goldIds, predIds, biPredicate);
  }

  private <E extends PubTatorMentionAnnotation> Collection<String> uniqueIds(Collection<E> list) {
    Set<String> keys = Sets.newHashSet();
    for(E e: list) {
      for(String conceptId: e.getConceptIds()) {
        String key = e.getId() + "\t" + conceptId;
        keys.add(key);
      }
    }
    return keys;
  }

  private void evalMention() {
    List<PubTatorMentionAnnotation> goldMentions = getAllMentions(goldDocuments, "Disease");
    List<PubTatorMentionAnnotation> predMentions = getAllMentions(predDocuments, "Disease");
    final BiPredicate<PubTatorMentionAnnotation, PubTatorMentionAnnotation> biPredicate =
        (m1, m2) -> m1.getId().equals(m2.getId())
            && m1.getType().equals(m2.getType())
            && m1.getStart() == m2.getStart()
            && m1.getEnd() == m2.getEnd();
    diseaseMentionStats = eval(goldMentions, predMentions, biPredicate);

    goldMentions = getAllMentions(goldDocuments, "Chemical");
    predMentions = getAllMentions(predDocuments, "Chemical");
    chemicalMentionStats = eval(goldMentions, predMentions, biPredicate);
  }

  private void evalCdr() {
    List<PubTatorRelationAnnotation> goldRelations = getAllRelations(goldDocuments, "CID");
    List<PubTatorRelationAnnotation> predRelations = getAllRelations(predDocuments, "CID");

    final BiPredicate<PubTatorRelationAnnotation, PubTatorRelationAnnotation> biPredicate =
        (r1, r2) -> r1.getId().equals(r2.getId())
                && r1.getType().equals(r2.getType())
                && (r1.getConceptId1().equals(r2.getConceptId1())
                && r1.getConceptId2().equals(r2.getConceptId2()))
                || (r1.getConceptId2().equals(r2.getConceptId1())
                && r1.getConceptId1().equals(r2.getConceptId2()));
    cdrStats =  eval(goldRelations, predRelations, biPredicate);
  }

  private <E> PrecisionRecallStats eval(Collection<E> golds, Collection<E> preds, BiPredicate<E, E>
      biPredicate) {
    PrecisionRecallStats<E> stats1 = new PrecisionRecallStats();
    for (E gold : golds) {
      Optional<E> o = find(gold, preds, biPredicate);
      if (o.isPresent()) {
        stats1.incrementTP(find(o.get(), golds, biPredicate).get());
      } else {
        stats1.incrementFN(find(gold, golds, biPredicate).get());
      }
    }
    PrecisionRecallStats stats2 = new PrecisionRecallStats();
    for (E pred : preds) {
      Optional<E> o = find(pred, golds, biPredicate);
      if (o.isPresent()) {
        stats2.incrementTP(o.get());
      } else {
        stats2.incrementFP(find(pred, golds, biPredicate).get());
      }
    }
    checkArgument(stats1.getTP() == stats1.getTP(), "Error");
    return new PrecisionRecallStats<E>(stats1.getTPs(), stats2.getFPs(), stats1.getFNs());
  }

  private <E> Optional<E> find(E target, Collection<E> list, BiPredicate<E, E> biPredicate) {
    return list.stream()
        .filter(r -> biPredicate.test(r, target))
        .findFirst();
  }

  private List<PubTatorRelationAnnotation> getAllRelations(Collection<PubTatorDocument> documents,
      String type) {
    return documents.stream()
        .flatMap(d -> d.getRelations().stream())
        .filter(r -> r.getType().equals(type))
        .collect(Collectors.toList());
  }

  private List<PubTatorMentionAnnotation> getAllMentions(Collection<PubTatorDocument> documents,
      String type) {
    return documents.stream()
        .flatMap(d -> d.getMentions().stream())
        .filter(m -> m.getType().equals(type))
        .collect(Collectors.toList());
  }
}
