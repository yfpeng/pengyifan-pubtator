package com.pengyifan.pubtator.io;

import com.pengyifan.pubtator.PubTatorDocument;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class PubTatorLoader2Test extends TestCase {

  private static final String line1 = "26094|t|Antihypertensive drugs and depression: a " +
      "reappraisal.\n" +
      "26094|a|Eighty-nine new referral hypertensive out-patients and 46 new referral " +
      "non-hypertensive chronically physically ill out-patients completed a mood rating scale at" +
      " regular intervals for one year. The results showed a high prevalence of depression in " +
      "both groups of patients, with no preponderance in the hypertensive group. Hypertensive " +
      "patients with psychiatric histories had a higher prevalence of depression than the " +
      "comparison patients. This was accounted for by a significant number of depressions " +
      "occurring in methyl dopa treated patients with psychiatric histories.\n" +
      "26094\t451\t461\tdepression\tDisease\tD003866\n" +
      "26094\t542\t553\tdepressions\tDisease\tD003866\n" +
      "26094\t567\t578\tmethyl dopa\tChemical\tD008750\n" +
      "26094\t601\t612\tpsychiatric\tDisease\tD001523\n" +
      "26094\tCID\tD008750\tD003866\n";

  private static final String line2 = "26094|t|Antihypertensive drugs and depression: a " +
      "reappraisal.\n" +
      "26094|a|Eighty-nine new referral hypertensive out-patients and 46 new referral " +
      "non-hypertensive chronically physically ill out-patients completed a mood rating scale at" +
      " regular intervals for one year. The results showed a high prevalence of depression in " +
      "both groups of patients, with no preponderance in the hypertensive group. Hypertensive " +
      "patients with psychiatric histories had a higher prevalence of depression than the " +
      "comparison patients. This was accounted for by a significant number of depressions " +
      "occurring in methyl dopa treated patients with psychiatric histories.\n";

  private static final String line3 = "26094|t|Antihypertensive drugs and depression: a " +
      "reappraisal.\n";

  private static final String line4 = "26094|t|Antihypertensive drugs and depression: a " +
      "reappraisal.\n" +
      "26094|a|Eighty-nine new referral hypertensive out-patients and 46 new referral " +
      "non-hypertensive chronically physically ill out-patients completed a mood rating scale at" +
      " regular intervals for one year. The results showed a high prevalence of depression in " +
      "both groups of patients, with no preponderance in the hypertensive group. Hypertensive " +
      "patients with psychiatric histories had a higher prevalence of depression than the " +
      "comparison patients. This was accounted for by a significant number of depressions " +
      "occurring in methyl dopa treated patients with psychiatric histories.\n" +
      "26094\t451\t461\tdepression\tDisease\tD003866\n" +
      "26094\t542\t553\tdepressions\tDisease\tD003866\n" +
      "26094\t567\t578\tmethyl dopa\tChemical\tD008750\n" +
      "26094\t601\t612\tpsychiatric\tDisease\tD001523\n" +
      "26094\tCID\tD008750\tD003867\n";

  private static final String line5 = "error";

  private static final String line6 = "26094|t|Antihypertensive drugs and depression: a " +
      "reappraisal.\n" +
      "26094|a|Eighty-nine new referral hypertensive out-patients and 46 new referral " +
      "non-hypertensive chronically physically ill out-patients completed a mood rating scale at" +
      " regular intervals for one year. The results showed a high prevalence of depression in " +
      "both groups of patients, with no preponderance in the hypertensive group. Hypertensive " +
      "patients with psychiatric histories had a higher prevalence of depression than the " +
      "comparison patients. This was accounted for by a significant number of depressions " +
      "occurring in methyl dopa treated patients with psychiatric histories.\n" +
      "26094\t451\t463\tdepression\tDisease\tD003866\n";

  private static final String line7 = "26094|t|Antihypertensive drugs and depression: a " +
      "reappraisal.\n" +
      "26094|a|Eighty-nine new referral hypertensive out-patients and 46 new referral " +
      "non-hypertensive chronically physically ill out-patients completed a mood rating scale at" +
      " regular intervals for one year. The results showed a high prevalence of depression in " +
      "both groups of patients, with no preponderance in the hypertensive group. Hypertensive " +
      "patients with psychiatric histories had a higher prevalence of depression than the " +
      "comparison patients. This was accounted for by a significant number of depressions " +
      "occurring in methyl dopa treated patients with psychiatric histories.\n" +
      "26094\t451\t461\tdepression\tDisease\tD003866\n" +
      "26094\t542\t553\tdepressions\tDisease\tD003866\n" +
      "26094\t567\t578\tmethyl dopa\tChemical\tD008750\n" +
      "26094\t601\t612\tpsychiatric\tDisease\tD001523\n" +
      "26094\tCID\tD008750\tD003866\n\n"+
      "26094|t|Antihypertensive drugs and depression: a " +
      "reappraisal.\n" +
      "26094|a|Eighty-nine new referral hypertensive out-patients and 46 new referral " +
      "non-hypertensive chronically physically ill out-patients completed a mood rating scale at" +
      " regular intervals for one year. The results showed a high prevalence of depression in " +
      "both groups of patients, with no preponderance in the hypertensive group. Hypertensive " +
      "patients with psychiatric histories had a higher prevalence of depression than the " +
      "comparison patients. This was accounted for by a significant number of depressions " +
      "occurring in methyl dopa treated patients with psychiatric histories.\n" +
      "26094\t451\t461\tdepression\tDisease\tD003866\n" +
      "26094\t542\t553\tdepressions\tDisease\tD003866\n" +
      "26094\t567\t578\tmethyl dopa\tChemical\tD008750\n" +
      "26094\t601\t612\tpsychiatric\tDisease\tD001523\n" +
      "26094\tCID\tD008750\tD003866\n";

  public void testRead() throws Exception {
    testRead(line7);
  }

  private void testRead(String line) throws IOException {
    PubTatorLoader2 loader = new PubTatorLoader2(new StringReader(line));
    List<PubTatorDocument> docs = loader.read();
    loader.close();

    if (loader.hasErrors()) {
      System.out.println(loader.getErrorMessage());
    }

    for(PubTatorDocument doc: docs) {
      System.out.println(doc.toPubTatorString());
      System.out.println();
    }
  }
}