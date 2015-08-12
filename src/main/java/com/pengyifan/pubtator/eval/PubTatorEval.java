package com.pengyifan.pubtator.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;
import org.apache.commons.lang3.Range;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class PubTatorEval {
  private final List<PubTatorDocument> goldDocuments;
  private final List<PubTatorDocument> predDocuments;

  PrecisionRecallStats<PubTatorMentionAnnotation> diseaseMentionStats;
  PrecisionRecallStats<PubTatorMentionAnnotation> chemicalMentionStats;

  PrecisionRecallStats<PubTatorMentionAnnotation> diseaseApproMentionStats;
  PrecisionRecallStats<PubTatorMentionAnnotation> chemicalApproMentionStats;

  PrecisionRecallStats<PubTatorRelationAnnotation> cdrStats;

  PrecisionRecallStats<PubTatorRelationAnnotation> cdrWithMentionStats;
  PrecisionRecallStats<PubTatorRelationAnnotation> cdrWithApproMentionStats;

  PrecisionRecallStats<String> diseaseIdStats;
  PrecisionRecallStats<String> chemicalIdStats;

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

  /**
   * @param mode 1-dner, 2-cid, 3-all
   * @return
   */
  public String getResult(int mode, EvalDisplay display) {
    if (mode == 0) {
      return display.getDiseaseResult(this);
    } else if (mode == 1) {
      return display.getCIDResult(this);
    } else {
      return display.getAllResult(this);
    }
  }

  public void eval() {
    evalMention();
    evalCdr();
    evalId();
    cdrWithMentionStats = evalCdrWithMention(mentionStrictBiPredicate);
    cdrWithApproMentionStats = evalCdrWithMention(mentionApproxBiPredicate);
  }

  public PrecisionRecallStats<String> getChemicalIdStats() {
    return chemicalIdStats;
  }

  public PrecisionRecallStats<PubTatorMentionAnnotation> getDiseaseMentionStats() {
    return diseaseMentionStats;
  }

  public PrecisionRecallStats<PubTatorMentionAnnotation> getChemicalMentionStats() {
    return chemicalMentionStats;
  }

  public PrecisionRecallStats<PubTatorMentionAnnotation> getDiseaseApproMentionStats() {
    return diseaseApproMentionStats;
  }

  public PrecisionRecallStats<PubTatorMentionAnnotation> getChemicalApproMentionStats() {
    return chemicalApproMentionStats;
  }

  public PrecisionRecallStats<PubTatorRelationAnnotation> getCdrStats() {
    return cdrStats;
  }

  public PrecisionRecallStats<PubTatorRelationAnnotation> getCdrWithMentionStats() {
    return cdrWithMentionStats;
  }

  public PrecisionRecallStats<PubTatorRelationAnnotation> getCdrWithApproMentionStats() {
    return cdrWithApproMentionStats;
  }

  public PrecisionRecallStats<String> getDiseaseIdStats() {
    return diseaseIdStats;
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
    Set<PubTatorRelationAnnotation> relations = documents.stream()
        .flatMap(d -> d.getRelations().stream())
        .filter(r -> r.getType().equals(type))
        .collect(Collectors.toSet());
    return Lists.newArrayList(relations);
  }

  private List<PubTatorMentionAnnotation> getAllMentions(Collection<PubTatorDocument> documents,
      String type) {
    Set<PubTatorMentionAnnotation> mentions = documents.stream()
        .flatMap(d -> d.getMentions().stream())
        .filter(m -> m.getType().equals(type))
        .collect(Collectors.toSet());
    return Lists.newArrayList(mentions);
  }
}