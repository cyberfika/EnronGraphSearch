// data.js — synthetic Enron-flavored email corpus
// Used to populate the analyzer with realistic-looking edges + weights.
// Names are stylized references; not the real corpus.

window.ENRON_PEOPLE = [
  { e: "kenneth.lay@enron.com",       d: "Chairman" },
  { e: "jeffrey.skilling@enron.com",  d: "President & COO" },
  { e: "andrew.fastow@enron.com",     d: "CFO" },
  { e: "richard.causey@enron.com",    d: "Chief Accounting" },
  { e: "rebecca.mark@enron.com",      d: "International" },
  { e: "greg.whalley@enron.com",      d: "Trading" },
  { e: "louise.kitchen@enron.com",    d: "EnronOnline" },
  { e: "john.lavorato@enron.com",     d: "Trading" },
  { e: "kevin.presto@enron.com",      d: "East Power" },
  { e: "sara.shackleton@enron.com",   d: "Legal" },
  { e: "mark.taylor@enron.com",       d: "Legal" },
  { e: "jeff.dasovich@enron.com",     d: "Govt Affairs" },
  { e: "richard.shapiro@enron.com",   d: "Govt Affairs" },
  { e: "steven.kean@enron.com",       d: "Govt Affairs" },
  { e: "james.derrick@enron.com",     d: "General Counsel" },
  { e: "mark.haedicke@enron.com",     d: "Legal Risk" },
  { e: "vince.kaminski@enron.com",    d: "Research" },
  { e: "stinson.gibner@enron.com",    d: "Research" },
  { e: "tanya.tamarchenko@enron.com", d: "Research" },
  { e: "phillip.allen@enron.com",     d: "West Gas" },
  { e: "matthew.lenhart@enron.com",   d: "West Gas" },
  { e: "mike.grigsby@enron.com",      d: "West Gas" },
  { e: "monique.sanchez@enron.com",   d: "West Gas" },
  { e: "kam.keiser@enron.com",        d: "Trading Ops" },
  { e: "hunter.shively@enron.com",    d: "Central Gas" },
  { e: "darron.giron@enron.com",      d: "Central Gas" },
  { e: "scott.neal@enron.com",        d: "Gas Marketing" },
  { e: "kay.mann@enron.com",          d: "Legal Contracts" },
  { e: "tana.jones@enron.com",        d: "Legal" },
  { e: "marie.heard@enron.com",       d: "Legal" },
  { e: "jane.tholt@enron.com",        d: "Trading" },
  { e: "chris.germany@enron.com",     d: "East Gas" },
  { e: "drew.fossum@enron.com",       d: "Pipeline" },
  { e: "kimberly.watson@enron.com",   d: "Pipeline" },
  { e: "susan.scott@enron.com",       d: "Pipeline" },
  { e: "lynn.blair@enron.com",        d: "Pipeline" },
  { e: "stanley.horton@enron.com",    d: "Pipeline" },
  { e: "rod.hayslett@enron.com",      d: "Pipeline" },
  { e: "michelle.cash@enron.com",     d: "HR" },
  { e: "cindy.olson@enron.com",       d: "HR" },
  { e: "joannie.williamson@enron.com",d: "Office Mgmt" },
  { e: "rosalee.fleming@enron.com",   d: "Exec Asst" },
  { e: "elizabeth.tilney@enron.com",  d: "Communications" },
  { e: "mark.palmer@enron.com",       d: "Press" },
  { e: "karen.denne@enron.com",       d: "Comms" },
];

// Build a directed weighted edge list. Weights are the "frequência de mensagens"
// — chosen to produce believable hubs (executives + legal + research clusters).
//
// Format: [fromIndex, toIndex, weight]
window.ENRON_EDGES = (function () {
  const P = window.ENRON_PEOPLE;
  const idx = {};
  P.forEach((p, i) => (idx[p.e] = i));
  const E = [];
  const add = (a, b, w) => E.push([idx[a], idx[b], w]);

  // Executive spine
  add("kenneth.lay@enron.com", "jeffrey.skilling@enron.com", 47);
  add("jeffrey.skilling@enron.com", "kenneth.lay@enron.com", 39);
  add("kenneth.lay@enron.com", "rosalee.fleming@enron.com", 88);
  add("rosalee.fleming@enron.com", "kenneth.lay@enron.com", 12);
  add("kenneth.lay@enron.com", "elizabeth.tilney@enron.com", 18);
  add("kenneth.lay@enron.com", "steven.kean@enron.com", 26);
  add("kenneth.lay@enron.com", "james.derrick@enron.com", 14);
  add("jeffrey.skilling@enron.com", "andrew.fastow@enron.com", 31);
  add("andrew.fastow@enron.com", "jeffrey.skilling@enron.com", 24);
  add("jeffrey.skilling@enron.com", "richard.causey@enron.com", 19);
  add("richard.causey@enron.com", "jeffrey.skilling@enron.com", 22);
  add("jeffrey.skilling@enron.com", "greg.whalley@enron.com", 28);
  add("greg.whalley@enron.com", "jeffrey.skilling@enron.com", 17);
  add("jeffrey.skilling@enron.com", "louise.kitchen@enron.com", 16);
  add("jeffrey.skilling@enron.com", "rebecca.mark@enron.com", 9);

  // Trading desk cluster
  add("greg.whalley@enron.com", "john.lavorato@enron.com", 41);
  add("john.lavorato@enron.com", "greg.whalley@enron.com", 35);
  add("john.lavorato@enron.com", "kevin.presto@enron.com", 23);
  add("kevin.presto@enron.com", "john.lavorato@enron.com", 19);
  add("john.lavorato@enron.com", "louise.kitchen@enron.com", 14);
  add("louise.kitchen@enron.com", "john.lavorato@enron.com", 12);
  add("kevin.presto@enron.com", "jane.tholt@enron.com", 11);
  add("jane.tholt@enron.com", "kevin.presto@enron.com", 8);

  // West gas cluster (Phillip Allen hub)
  add("phillip.allen@enron.com", "matthew.lenhart@enron.com", 52);
  add("matthew.lenhart@enron.com", "phillip.allen@enron.com", 44);
  add("phillip.allen@enron.com", "mike.grigsby@enron.com", 33);
  add("mike.grigsby@enron.com", "phillip.allen@enron.com", 29);
  add("phillip.allen@enron.com", "monique.sanchez@enron.com", 21);
  add("monique.sanchez@enron.com", "phillip.allen@enron.com", 18);
  add("phillip.allen@enron.com", "kam.keiser@enron.com", 16);
  add("phillip.allen@enron.com", "scott.neal@enron.com", 14);
  add("scott.neal@enron.com", "phillip.allen@enron.com", 9);
  add("matthew.lenhart@enron.com", "mike.grigsby@enron.com", 12);
  add("mike.grigsby@enron.com", "matthew.lenhart@enron.com", 10);

  // Central / east gas
  add("hunter.shively@enron.com", "darron.giron@enron.com", 27);
  add("darron.giron@enron.com", "hunter.shively@enron.com", 19);
  add("hunter.shively@enron.com", "scott.neal@enron.com", 15);
  add("chris.germany@enron.com", "scott.neal@enron.com", 18);
  add("scott.neal@enron.com", "chris.germany@enron.com", 13);
  add("chris.germany@enron.com", "kay.mann@enron.com", 11);

  // Legal cluster
  add("sara.shackleton@enron.com", "mark.taylor@enron.com", 38);
  add("mark.taylor@enron.com", "sara.shackleton@enron.com", 31);
  add("sara.shackleton@enron.com", "tana.jones@enron.com", 26);
  add("tana.jones@enron.com", "sara.shackleton@enron.com", 22);
  add("mark.taylor@enron.com", "marie.heard@enron.com", 19);
  add("marie.heard@enron.com", "mark.taylor@enron.com", 17);
  add("kay.mann@enron.com", "mark.taylor@enron.com", 24);
  add("mark.taylor@enron.com", "kay.mann@enron.com", 21);
  add("mark.haedicke@enron.com", "sara.shackleton@enron.com", 16);
  add("mark.haedicke@enron.com", "mark.taylor@enron.com", 14);
  add("james.derrick@enron.com", "mark.haedicke@enron.com", 9);
  add("james.derrick@enron.com", "sara.shackleton@enron.com", 7);

  // Research cluster
  add("vince.kaminski@enron.com", "stinson.gibner@enron.com", 34);
  add("stinson.gibner@enron.com", "vince.kaminski@enron.com", 28);
  add("vince.kaminski@enron.com", "tanya.tamarchenko@enron.com", 22);
  add("tanya.tamarchenko@enron.com", "vince.kaminski@enron.com", 17);
  add("vince.kaminski@enron.com", "jeffrey.skilling@enron.com", 8);
  add("vince.kaminski@enron.com", "andrew.fastow@enron.com", 6);

  // Govt affairs cluster
  add("steven.kean@enron.com", "richard.shapiro@enron.com", 29);
  add("richard.shapiro@enron.com", "steven.kean@enron.com", 24);
  add("steven.kean@enron.com", "jeff.dasovich@enron.com", 21);
  add("jeff.dasovich@enron.com", "steven.kean@enron.com", 18);
  add("richard.shapiro@enron.com", "jeff.dasovich@enron.com", 16);
  add("jeff.dasovich@enron.com", "richard.shapiro@enron.com", 13);
  add("steven.kean@enron.com", "karen.denne@enron.com", 11);

  // Pipeline cluster
  add("stanley.horton@enron.com", "drew.fossum@enron.com", 26);
  add("drew.fossum@enron.com", "stanley.horton@enron.com", 23);
  add("drew.fossum@enron.com", "kimberly.watson@enron.com", 19);
  add("kimberly.watson@enron.com", "drew.fossum@enron.com", 16);
  add("drew.fossum@enron.com", "susan.scott@enron.com", 14);
  add("susan.scott@enron.com", "drew.fossum@enron.com", 12);
  add("drew.fossum@enron.com", "lynn.blair@enron.com", 11);
  add("rod.hayslett@enron.com", "stanley.horton@enron.com", 9);
  add("stanley.horton@enron.com", "rod.hayslett@enron.com", 7);

  // Comms cluster
  add("elizabeth.tilney@enron.com", "mark.palmer@enron.com", 22);
  add("mark.palmer@enron.com", "elizabeth.tilney@enron.com", 18);
  add("elizabeth.tilney@enron.com", "karen.denne@enron.com", 16);
  add("karen.denne@enron.com", "mark.palmer@enron.com", 14);

  // HR / admin
  add("cindy.olson@enron.com", "michelle.cash@enron.com", 13);
  add("michelle.cash@enron.com", "cindy.olson@enron.com", 10);
  add("cindy.olson@enron.com", "kenneth.lay@enron.com", 8);
  add("joannie.williamson@enron.com", "kenneth.lay@enron.com", 6);

  // Cross-cluster bridges (these become the interesting "critical paths")
  add("andrew.fastow@enron.com", "richard.causey@enron.com", 18);
  add("richard.causey@enron.com", "andrew.fastow@enron.com", 14);
  add("andrew.fastow@enron.com", "mark.haedicke@enron.com", 7);
  add("louise.kitchen@enron.com", "phillip.allen@enron.com", 9);
  add("kay.mann@enron.com", "drew.fossum@enron.com", 8);
  add("greg.whalley@enron.com", "vince.kaminski@enron.com", 6);
  add("jeff.dasovich@enron.com", "mark.palmer@enron.com", 7);
  add("richard.shapiro@enron.com", "mark.palmer@enron.com", 5);
  add("rebecca.mark@enron.com", "kenneth.lay@enron.com", 11);
  add("rebecca.mark@enron.com", "jeffrey.skilling@enron.com", 8);

  return E;
})();
