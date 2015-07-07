package com.pengyifan.pubtator.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;
import org.apache.commons.lang3.Range;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public abstract class PubTatorEval {

  public static class DetailedEval extends PubTatorEval {

    public DetailedEval(List<PubTatorDocument> gold, List<PubTatorDocument> pred) {
      super(gold, pred);
    }

    public String getNERResult() {
      ResultPrinter resultPrinter = new ResultPrinter();
      resultPrinter.printTitle();
      resultPrinter.printRow("Disease");
      resultPrinter.printRow("  Concept id matching", diseaseIdStats);
      resultPrinter.printRow("  Mention (Strict matching)", diseaseMentionStats);
      resultPrinter.printRow("  Mention (Appro. matching)", diseaseApproMentionStats);
      resultPrinter.printRow("Chemical");
      resultPrinter.printRow("  Concept id matching", chemicalIdStats);
      resultPrinter.printRow("  Mention (Strict matching)", chemicalMentionStats);
      resultPrinter.printRow("  Mention (Appro. matching)", chemicalApproMentionStats);
      return resultPrinter.toString();
    }

    public String getCIDResult() {
      ResultPrinter resultPrinter = new ResultPrinter();
      resultPrinter.printTitle();
      resultPrinter.printRow("CID");
      resultPrinter.printRow("  Concept id matching", cdrStats);
      resultPrinter.printRow("  Mention (Strict matching)", cdrWithMentionStats);
      resultPrinter.printRow("  Mention (Appro matching)", cdrWithApproMentionStats);
      return resultPrinter.toString();
    }

    public String getResult() {
      ResultPrinter resultPrinter = new ResultPrinter();
      resultPrinter.printTitle();
      resultPrinter.printRow("Disease");
      resultPrinter.printRow("  Concept id matching", diseaseIdStats);
      resultPrinter.printRow("  Mention (Strict matching)", diseaseMentionStats);
      resultPrinter.printRow("  Mention (Appro. matching)", diseaseApproMentionStats);
      resultPrinter.printRow("Chemical");
      resultPrinter.printRow("  Concept id matching", chemicalIdStats);
      resultPrinter.printRow("  Mention (Strict matching)", chemicalMentionStats);
      resultPrinter.printRow("  Mention (Appro. matching)", chemicalApproMentionStats);
      resultPrinter.printRow("CID");
      resultPrinter.printRow("  Concept id matching", cdrStats);
      resultPrinter.printRow("  Mention (Strict matching)", cdrWithMentionStats);
      resultPrinter.printRow("  Mention (Appro matching)", cdrWithApproMentionStats);
      return resultPrinter.toString();
    }
  }

  public static class SimpleEval extends PubTatorEval {

    public SimpleEval(List<PubTatorDocument> gold, List<PubTatorDocument> pred) {
      super(gold, pred);
    }

    public String getNERResult() {
      ResultPrinter resultPrinter = new ResultPrinter();
      resultPrinter.printTitle();
      resultPrinter.printRow("Disease");
      resultPrinter.printRow("  Concept id matching", diseaseIdStats);
      resultPrinter.printRow("  Mention matching", diseaseMentionStats);
      return resultPrinter.toString();
    }

    public String getCIDResult() {
      ResultPrinter resultPrinter = new ResultPrinter();
      resultPrinter.printTitle();
      resultPrinter.printRow("CID", cdrStats);
      return resultPrinter.toString();
    }

    public String getResult() {
      ResultPrinter resultPrinter = new ResultPrinter();
      resultPrinter.printTitle();
      resultPrinter.printRow("Disease");
      resultPrinter.printRow("  Concept id matching", diseaseIdStats);
      resultPrinter.printRow("  Mention matching", diseaseMentionStats);
      resultPrinter.printRow("Chemical");
      resultPrinter.printRow("  Concept id matching", chemicalIdStats);
      resultPrinter.printRow("  Mention matching", chemicalMentionStats);
      resultPrinter.printRow("CID", cdrStats);
      return resultPrinter.toString();
    }
  }

  private final List<PubTatorDocument> goldDocuments;
  private final List<PubTatorDocument> predDocuments;

  PrecisionRecallStats diseaseMentionStats;
  PrecisionRecallStats chemicalMentionStats;

  PrecisionRecallStats diseaseApproMentionStats;
  PrecisionRecallStats chemicalApproMentionStats;

  PrecisionRecallStats cdrStats;

  PrecisionRecallStats cdrWithMentionStats;
  PrecisionRecallStats cdrWithApproMentionStats;

  PrecisionRecallStats diseaseIdStats;
  PrecisionRecallStats chemicalIdStats;

  private static final BiPredicate<PubTatorMentionAnnotation, PubTatorMentionAnnotation>
      mentionStrictBiPredicate =
      (m1, m2) -> m1.getId().equals(m2.getId())
          && m1.getType().equals(m2.getType())
          && m1.getStart() == m2.getStart()
          && m1.getEnd() == m2.getEnd();

  private static final BiPredicate<PubTatorMentionAnnotation, PubTatorMentionAnnotation>
      mentionApproxBiPredicate =
      (m1, m2) -> m1.getId().equals(m2.getId())
          && m1.getType().equals(m2.getType())
          && Range.between(m1.getStart(), m1.getEnd()).isOverlappedBy(
          Range.between(m2.getStart(), m2.getEnd()));

  private static final BiPredicate<PubTatorRelationAnnotation, PubTatorRelationAnnotation>
      cdiBiPredicate =
      (r1, r2) -> r1.getId().equals(r2.getId())
          && r1.getType().equals(r2.getType())
          && (r1.getConceptId1().equals(r2.getConceptId1())
          && r1.getConceptId2().equals(r2.getConceptId2()))
          || (r1.getConceptId2().equals(r2.getConceptId1())
          && r1.getConceptId1().equals(r2.getConceptId2()));

  public PubTatorEval(List<PubTatorDocument> gold, List<PubTatorDocument> pred) {
    this.goldDocuments = gold;
    this.predDocuments = pred;
  }

  public abstract String getNERResult();

  public abstract String getCIDResult();

  public abstract String getResult();

  public void eval() {
    evalMention();
    evalCdr();
    evalId();
    cdrWithMentionStats = evalCdrWithMention(mentionStrictBiPredicate);
    cdrWithApproMentionStats = evalCdrWithMention(mentionApproxBiPredicate);
  }

  private void evalId() {
    List<String> goldIds = uniqueIds(getAllMentions(goldDocuments, "Disease"));
    List<String> predIds = uniqueIds(getAllMentions(predDocuments, "Disease"));

    final BiPredicate<String, String> biPredicate = (m1, m2) -> m1.equals(m2);
    diseaseIdStats = eval(goldIds, predIds, (m1, m2) -> m1.equals(m2));

    goldIds = uniqueIds(getAllMentions(goldDocuments, "Chemical"));
    predIds = uniqueIds(getAllMentions(predDocuments, "Chemical"));
    chemicalIdStats = eval(goldIds, predIds, biPredicate);
  }

  private <E extends PubTatorMentionAnnotation> List<String> uniqueIds(Collection<E> list) {
    Set<String> keys = Sets.newHashSet();
    for (E e : list) {
      for (String conceptId : e.getConceptIds()) {
        String key = e.getId() + "\t" + conceptId;
        keys.add(key);
      }
    }
    return Lists.newArrayList(keys);
  }

  private void evalMention() {
    List<PubTatorMentionAnnotation> goldMentions = getAllMentions(goldDocuments, "Disease");
    List<PubTatorMentionAnnotation> predMentions = getAllMentions(predDocuments, "Disease");

    diseaseMentionStats = eval(goldMentions, predMentions, mentionStrictBiPredicate);
    diseaseApproMentionStats = eval(goldMentions, predMentions, mentionApproxBiPredicate);

    goldMentions = getAllMentions(goldDocuments, "Chemical");
    predMentions = getAllMentions(predDocuments, "Chemical");
    chemicalMentionStats = eval(goldMentions, predMentions, mentionStrictBiPredicate);
    chemicalApproMentionStats = eval(goldMentions, predMentions, mentionApproxBiPredicate);
  }

  private PrecisionRecallStats<PubTatorRelationAnnotation> evalCdrWithMention(
      BiPredicate<PubTatorMentionAnnotation, PubTatorMentionAnnotation> biPredicate) {
    List<PubTatorRelationAnnotation> goldRelations = getAllRelations(goldDocuments, "CID");
    List<PubTatorRelationAnnotation> predRelations = getAllRelations(predDocuments, "CID");

    List<PubTatorMentionAnnotation> goldMentions = getAllMentions(goldDocuments, "Disease");
    List<PubTatorMentionAnnotation> predMentions = getAllMentions(predDocuments, "Disease");
    goldMentions.addAll(getAllMentions(goldDocuments, "Chemical"));
    predMentions.addAll(getAllMentions(predDocuments, "Chemical"));

    PrecisionRecallStats<PubTatorRelationAnnotation> stats = new PrecisionRecallStats<>();
    boolean[] foundInGold = new boolean[predRelations.size()];
    for (PubTatorRelationAnnotation gold : goldRelations) {
      boolean found = false;
      for (int i = 0; i < predRelations.size(); i++) {
        PubTatorRelationAnnotation pred = predRelations.get(i);
        if (cdiBiPredicate.test(gold, pred) &&
            findMention(gold, pred, goldMentions, predMentions, biPredicate)) {
          foundInGold[i] = true;
          found = true;
        }
      }
      if (found) {
        stats.incrementTP(gold);
      } else {
        stats.incrementFN(gold);
      }
    }
    for (int i = 0; i < foundInGold.length; i++) {
      if (!foundInGold[i]) {
        stats.incrementFP(predRelations.get(i));
      }
    }
    return stats;
  }

  private boolean findMention(PubTatorRelationAnnotation gold,
      PubTatorRelationAnnotation pred,
      List<PubTatorMentionAnnotation> goldMentions,
      List<PubTatorMentionAnnotation> predMentions,
      BiPredicate<PubTatorMentionAnnotation, PubTatorMentionAnnotation> biPredicate) {
    // concept 1
    List<PubTatorMentionAnnotation> goldConceptMentions = goldMentions.stream()
        .filter(m -> m.getId().equals(gold.getId())
                && m.getConceptIds().contains(gold.getConceptId1())
        ).collect(Collectors.toList());
    List<PubTatorMentionAnnotation> predConceptMentions = predMentions.stream()
        .filter(m -> m.getId().equals(pred.getId())
                && m.getConceptIds().contains(pred.getConceptId1())
        ).collect(Collectors.toList());
    boolean hasConcept1 = false;
    for (PubTatorMentionAnnotation goldMention : goldConceptMentions) {
      if (find(goldMention, predConceptMentions, biPredicate).isPresent()) {
        hasConcept1 = true;
      }
      for (PubTatorMentionAnnotation predMention : predConceptMentions) {
        if (biPredicate.test(goldMention, predMention)) {
          hasConcept1 = true;
        }
      }
    }
    if (!hasConcept1) {
      return false;
    }
    // concept 2
    goldConceptMentions = goldMentions.stream()
        .filter(m -> m.getId().equals(gold.getId())
                && m.getConceptIds().contains(gold.getConceptId2())
        ).collect(Collectors.toList());
    predConceptMentions = predMentions.stream()
        .filter(m -> m.getId().equals(pred.getId())
                && m.getConceptIds().contains(pred.getConceptId2())
        ).collect(Collectors.toList());
    for (PubTatorMentionAnnotation goldMention : goldConceptMentions) {
      for (PubTatorMentionAnnotation predMention : predConceptMentions) {
        if (biPredicate.test(goldMention, predMention)) {
          return true;
        }
      }
    }
    return false;
  }

  private void evalCdr() {
    List<PubTatorRelationAnnotation> goldRelations = getAllRelations(goldDocuments, "CID");
    List<PubTatorRelationAnnotation> predRelations = getAllRelations(predDocuments, "CID");
    cdrStats = eval(goldRelations, predRelations, cdiBiPredicate);
  }

  private <E> PrecisionRecallStats eval(List<E> golds, List<E> preds,
      BiPredicate<E, E> biPredicate) {
    PrecisionRecallStats<E> stats = new PrecisionRecallStats<>();
    boolean[] foundInGold = new boolean[preds.size()];
    for (E gold : golds) {
      boolean found = false;
      for (int i = 0; i < preds.size(); i++) {
        if (biPredicate.test(gold, preds.get(i))) {
          foundInGold[i] = true;
          found = true;
        }
      }
      if (found) {
        stats.incrementTP(gold);
      } else {
        stats.incrementFN(gold);
      }
    }
    for (int i = 0; i < foundInGold.length; i++) {
      if (!foundInGold[i]) {
        stats.incrementFP(preds.get(i));
      }
    }
    return stats;
  }

  private <E> Optional<E> find(E target, Collection<E> list, BiPredicate<E, E> biPredicate) {
    return list.stream()
        .filter(r -> biPredicate.test(target, r))
        .findAny();
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
