import React, { useEffect, useState } from "react";
import {
  ShieldCheck,
  LayoutDashboard,
  ClipboardPlus,
  FileText,
  Activity,
  Plus,
  Trash2,
  ArrowRight,
  AlertTriangle,
  Download,
  Pill,
  Menu,
  X,
} from "lucide-react";
import { api } from "./api";
const initialMedicine = () => ({
  id: crypto.randomUUID(),
  name: "",
  ingredient: "",
  batch: "",
  manufacturer: "",
});
const initialIllness = () => ({
  id: crypto.randomUUID(),
  name: "",
});
const nav = [
  ["Dashboard", LayoutDashboard],
  ["New Analysis", ClipboardPlus],
  ["Reports", FileText],
];
function App() {
  const [backendStatus, setBackendStatus] = useState("Checking backend...");
  const [backendData, setBackendData] = useState({
    medicines: [],
    illnesses: [],
  });
  const [saveStatus, setSaveStatus] = useState("");
  const [savingMedicines, setSavingMedicines] = useState(false);
  useEffect(() => {
    let cancelled = false;
    async function checkBackend() {
      try {
        const [, medicines, illnesses] = await Promise.all([
          api.health(),
          api.getMedicines(),
          api.getIllnesses(),
        ]);
        if (!cancelled) {
          setBackendData({
            medicines: Array.isArray(medicines) ? medicines : [],
            illnesses: Array.isArray(illnesses) ? illnesses : [],
          });
          setBackendStatus("Connected to backend");
        }
      } catch (error) {
        console.error("Backend connection error:", error);
        if (!cancelled) {
          setBackendStatus("Connection failed — check backend and CORS");
        }
      }
    }
    checkBackend();
    return () => {
      cancelled = true;
    };
  }, []);
  const [page, setPage] = useState("Dashboard");
  const [illnesses, setIllnesses] = useState([initialIllness()]);
  const [medicines, setMedicines] = useState([initialMedicine()]);
  const [report, setReport] = useState(null);
  const [history, setHistory] = useState(() => {
    try {
      const savedHistory = localStorage.getItem("pharmaguard-reports");
      return savedHistory ? JSON.parse(savedHistory) : [];
    } catch (error) {
      console.error("Could not load report history:", error);
      return [];
    }
  });
  useEffect(() => {
    try {
      localStorage.setItem(
        "pharmaguard-reports",
        JSON.stringify(history)
      );
    } catch (error) {
      console.error("Could not save report history:", error);
    }
  }, [history]);
  const [menu, setMenu] = useState(false);
  const update = (setter, id, key, value) => {
    setter((previous) =>
      previous.map((item) =>
        item.id === id ? { ...item, [key]: value } : item
      )
    );
  };
  const saveMedicines = async () => {
    const entries = medicines.filter((item) => item.name.trim());
    if (entries.length === 0) {
      setSaveStatus("Enter at least one medicine name before saving.");
      return;
    }
    const missingBatch = entries.some((item) => !item.batch.trim());
    if (missingBatch) {
      setSaveStatus("Enter a batch number for every medicine you want to save.");
      return;
    }
    setSavingMedicines(true);
    setSaveStatus("");
    try {
      await api.createMedicines(
        entries.map(({ name, ingredient, batch, manufacturer }) => ({
          name: name.trim(),
          ingredient: ingredient.trim(),
          batch: batch.trim(),
          manufacturer: manufacturer.trim(),
        }))
      );
      const savedMedicines = await api.getMedicines();
      setBackendData((previous) => ({
        ...previous,
        medicines: Array.isArray(savedMedicines) ? savedMedicines : previous.medicines,
      }));
      setSaveStatus(`Successfully saved ${entries.length} medicine(s) to the database.`);
    } catch (error) {
      console.error("Could not save medicines:", error);
      setSaveStatus(`Could not save medicines: ${error.message}. Check the backend console for details.`);
    } finally {
      setSavingMedicines(false);
    }
  };
  const analyze = async () => {
    const selectedIllnesses = illnesses
      .map((item) => item.name.trim())
      .filter(Boolean);
    const selectedMedicines = medicines.filter(
      (item) => item.name.trim()
    );

    if (
      selectedIllnesses.length === 0 ||
      selectedMedicines.length === 0
    ) {
      alert("Enter at least one illness and one medicine.");
      return;
    }

    if (selectedMedicines.some((item) => !item.batch.trim())) {
      alert("Enter a batch number for every medicine.");
      return;
    }

    if (
      selectedMedicines.some((item) => !item.manufacturer.trim())
    ) {
      alert("Enter the manufacturer for every medicine.");
      return;
    }

    try {
      setSaveStatus("Generating report and verifying batches...");

      const savedMedicines = await api.createMedicines(
        selectedMedicines.map((item) => ({
          name: item.name.trim(),
          ingredient: item.ingredient.trim(),
          batch: item.batch.trim(),
          manufacturer: item.manufacturer.trim(),
        }))
      );

      const savedIllnesses = await api.createIllnesses(
        selectedIllnesses.map((name) => ({ name }))
      );

      const medicineIds = savedMedicines.map((item) => item.id);
      const illnessIds = savedIllnesses.map((item) => item.id);

      if (
        medicineIds.some((id) => id == null) ||
        illnessIds.some((id) => id == null)
      ) {
        throw new Error("The backend did not return all record IDs.");
      }

      const backendReport = await api.generateAnalysis(
        medicineIds,
        illnessIds
      );

      const counterfeitResults = await Promise.all(
        selectedMedicines.map(async (medicine) => {
          try {
            return await api.verifyCounterfeit(
              medicine.name.trim(),
              medicine.batch.trim(),
              medicine.manufacturer.trim()
            );
          } catch (error) {
            return {
              medicineName: medicine.name.trim(),
              batchNumber: medicine.batch.trim(),
              manufacturer: medicine.manufacturer.trim(),
              status: "ERROR",
              explanation: `Verification request failed: ${error.message}`,
              evidenceSource: "API request failed",
            };
          }
        })
      );

      const result = {
        ...backendReport,
        id: crypto.randomUUID(),
        date: new Date().toLocaleString(),
        conditions: Array.isArray(backendReport.illnesses)
          ? backendReport.illnesses.map((item) => item.name)
          : selectedIllnesses,
        drugs: Array.isArray(backendReport.medicines)
          ? backendReport.medicines
          : selectedMedicines,
        counterfeitResults,
        checks: (Array.isArray(backendReport.medicines)
          ? backendReport.medicines
          : selectedMedicines
        ).map((medicine, index) => ({
          name: medicine.name,
          indication:
            "Not evaluated unless supported by clinical evidence in the backend.",
          authenticity:
            counterfeitResults[index]?.status || "NOT_CHECKED",
        })),
        interactions: JSON.stringify(
          backendReport.interactionResults ?? [],
          null,
          2
        ),
      };

      setReport(result);
      setHistory((previous) => [result, ...previous]);
      setPage("Results");
      setSaveStatus("");

      try {
        const [allMedicines, allIllnesses] = await Promise.all([
          api.getMedicines(),
          api.getIllnesses(),
        ]);
        setBackendData({
          medicines: allMedicines,
          illnesses: allIllnesses,
        });
      } catch (refreshError) {
        console.error("Could not refresh dashboard counts:", refreshError);
      }
    } catch (error) {
      console.error("Analysis failed:", error);
      setSaveStatus(`Could not generate report: ${error.message}`);
    }
  };

  const card =
    "bg-white border border-slate-200 rounded-2xl shadow-sm";
  const inputClass =
    "mt-1 w-full min-w-0 border border-slate-200 rounded-xl p-3 outline-none focus:ring-2 focus:ring-teal-500 focus:border-teal-500";
  return (
    <div className="min-h-screen bg-slate-50 md:flex">
      {/* Sidebar */}
      <aside
        className={`${
          menu ? "block" : "hidden"
        } md:block w-full md:w-64 bg-[#102b43] text-white md:min-h-screen p-6 shrink-0`}
      >
        <div className="flex items-center gap-2 mb-12">
          <ShieldCheck className="text-teal-300" size={30} />
          <span className="font-bold text-xl whitespace-nowrap">
            Pharma <span className="text-teal-300">Guard</span>
          </span>
          <button
            className="ml-auto md:hidden"
            onClick={() => setMenu(false)}
            aria-label="Close navigation"
          >
            <X />
          </button>
        </div>
        <div className="text-xs uppercase tracking-widest text-slate-400 mb-3">
          Workspace
        </div>
        {nav.map(([name, Icon]) => (
          <button
            key={name}
            onClick={() => {
              setPage(name);
              setMenu(false);
            }}
            className={`w-full flex items-center gap-3 p-3 rounded-xl mb-2 text-left transition ${
              page === name
                ? "bg-teal-600"
                : "hover:bg-slate-700"
            }`}
          >
            <Icon size={19} />
            {name}
          </button>
        ))}
        <div className="mt-12 text-xs text-slate-400 leading-relaxed">
          Your health, our priority. Always verify medicine
          safety with a qualified healthcare professional.
        </div>
      </aside>
      {/* Main content */}
      <main className="flex-1 min-w-0">
        <div className="mx-5 mt-5 rounded-xl border border-slate-200 bg-white p-4 md:mx-10">
          <p className="font-semibold">{backendStatus}</p>
          <p className="mt-2 text-sm text-slate-600">
            Saved medicines: {backendData.medicines.length}
          </p>
          <p className="text-sm text-slate-600">
            Saved illnesses: {backendData.illnesses.length}
          </p>
        </div>
        {/* Header — Demo Mode badge removed */}
        <header className="bg-white border-b border-slate-200 px-5 md:px-10 py-5 flex items-center gap-4">
          <button
            className="md:hidden"
            onClick={() => setMenu(true)}
            aria-label="Open navigation"
          >
            <Menu />
          </button>
          <div>
            <p className="text-sm text-slate-500">
              Pharma Guard | Medicine safety workspace
            </p>
            <h1 className="text-xl font-bold text-slate-900">
              {page === "Results" ? "Analysis Report" : page}
            </h1>
          </div>
        </header>
        <div className="p-5 md:p-8 max-w-7xl mx-auto">
          {/* Dashboard */}
          {page === "Dashboard" && (
            <>
              {/* Hero banner with background image */}
              <section
                className="relative overflow-hidden rounded-3xl mb-8 min-h-90 md:min-h-100 flex items-center bg-cover bg-center"
                style={{
                  backgroundImage: `
                    linear-gradient(
                      90deg,
                      rgba(3, 25, 43, 0.96) 0%,
                      rgba(5, 48, 67, 0.88) 48%,
                      rgba(5, 60, 75, 0.30) 100%
                    ),
                    url("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=1600&q=85")
                  `,
                }}
              >
                <div className="relative z-10 p-7 md:p-12 max-w-4xl">
                  <div className="text-cyan-300 text-sm md:text-base font-bold tracking-[0.2em] mb-4">
                    MEDICINE SAFETY PLATFORM
                  </div>
                  <h2 className="text-3xl md:text-5xl font-extrabold leading-tight mb-5 text-white">
                    Understand your medicines,
                    <br />
                    <span className="text-teal-300">
                      one report at a time.
                    </span>
                  </h2>
                  <p className="text-slate-100 max-w-2xl mb-7 text-base md:text-lg leading-relaxed">
                    Organize your conditions and medicines and
                    prepare for future evidence-based safety checks.
                  </p>
                  <button
                    onClick={() => setPage("New Analysis")}
                    className="bg-teal-500 hover:bg-teal-400 text-white px-6 py-3 rounded-xl font-semibold inline-flex items-center gap-2 transition"
                  >
                    <Plus size={19} />
                    Start New Analysis
                    <ArrowRight size={18} />
                  </button>
                  <div className="grid sm:grid-cols-3 gap-3 mt-8">
                    {[
                      {
                        title: "Check Suitability",
                        description: "Medicine and illness matching",
                        Icon: Pill,
                      },
                      {
                        title: "Verify Authenticity",
                        description: "Batch number screening",
                        Icon: ShieldCheck,
                      },
                      {
                        title: "Detect Interactions",
                        description: "Medicine combination review",
                        Icon: AlertTriangle,
                      },
                    ].map(({ title, description, Icon }) => (
                      <div
                        key={title}
                        className="bg-white/10 backdrop-blur-sm border border-white/15 rounded-xl p-4"
                      >
                        <Icon
                          className="text-teal-300 mb-2"
                          size={24}
                        />
                        <h3 className="text-white font-semibold text-sm">
                          {title}
                        </h3>
                        <p className="text-slate-200 text-xs mt-1">
                          {description}
                        </p>
                      </div>
                    ))}
                  </div>
                </div>
              </section>
              {/* Feature cards */}
              <div className="grid md:grid-cols-3 gap-5 mb-8">
                {[
                  {
                    title: "Suitability Review",
                    Icon: Pill,
                    description:
                      "Organize medicines by the conditions they may treat.",
                  },
                  {
                    title: "Authenticity Screening",
                    Icon: ShieldCheck,
                    description:
                      "Prepare batch number and manufacturer verification.",
                  },
                  {
                    title: "Interaction Review",
                    Icon: Activity,
                    description:
                      "Review potential interactions between medicines.",
                  },
                ].map(({ title, Icon, description }) => (
                  <div key={title} className={`${card} p-6`}>
                    <Icon
                      className="text-teal-700 mb-4"
                      size={30}
                    />
                    <h3 className="font-bold mb-2 text-slate-900">
                      {title}
                    </h3>
                    <p className="text-sm text-slate-500">
                      {description}
                    </p>
                  </div>
                ))}
              </div>
              {/* Recent analyses */}
              <div className={`${card} p-6`}>
                <div className="flex items-center justify-between gap-3 mb-3">
                  <h3 className="font-bold text-lg">
                    Recent Analyses
                  </h3>
                  {history.length > 0 && (
                    <button
                      onClick={() => setPage("Reports")}
                      className="text-teal-700 text-sm font-semibold"
                    >
                      View All
                    </button>
                  )}
                </div>
                <p className="text-sm text-slate-500">
                  {history.length
                    ? `${history.length} report(s) created in this session.`
                    : "No reports yet. Create your first medicine analysis."}
                </p>
              </div>
            </>
          )}
          {/* New Analysis */}
          {page === "New Analysis" && (
            <>
              <div className="mb-7">
                <h2 className="text-2xl font-bold mb-2 text-slate-900">
                  New Medicine Analysis
                </h2>
                <p className="text-slate-500">
                  Enter your illnesses and medicines to prepare a
                  structured safety report.
                </p>
              </div>
              {/* Illnesses */}
              <section className={`${card} p-6 mb-6`}>
                <div className="flex flex-wrap justify-between items-center gap-3 mb-5">
                  <h3 className="font-bold text-lg">
                    1. Your Illnesses or Conditions
                  </h3>
                  <button
                    onClick={() =>
                      setIllnesses((previous) => [
                        ...previous,
                        initialIllness(),
                      ])
                    }
                    className="text-teal-700 font-semibold flex gap-1 items-center"
                  >
                    <Plus size={18} />
                    Add Illness
                  </button>
                </div>
                {illnesses.map((illness, index) => (
                  <div
                    key={illness.id}
                    className="flex gap-3 mb-3"
                  >
                    <input
                      aria-label={`Illness ${index + 1}`}
                      placeholder={`Condition ${index + 1} (e.g., hypertension)`}
                      value={illness.name}
                      onChange={(event) =>
                        update(
                          setIllnesses,
                          illness.id,
                          "name",
                          event.target.value
                        )
                      }
                      className={inputClass}
                    />
                    <button
                      aria-label="Remove illness"
                      disabled={illnesses.length === 1}
                      onClick={() =>
                        setIllnesses((previous) =>
                          previous.filter(
                            (item) => item.id !== illness.id
                          )
                        )
                      }
                      className="text-rose-600 disabled:opacity-30"
                    >
                      <Trash2 size={19} />
                    </button>
                  </div>
                ))}
              </section>
              {/* Medicines */}
              <section className={`${card} p-6 mb-6`}>
                <div className="flex flex-wrap justify-between items-center gap-3 mb-5">
                  <h3 className="font-bold text-lg">
                    2. Your Medicines
                  </h3>
                  <button
                    onClick={() =>
                      setMedicines((previous) => [
                        ...previous,
                        initialMedicine(),
                      ])
                    }
                    className="text-teal-700 font-semibold flex gap-1 items-center"
                  >
                    <Plus size={18} />
                    Add Medicine
                  </button>
                </div>
                {medicines.map((medicine, index) => (
                  <div
                    key={medicine.id}
                    className="bg-slate-50 border border-slate-100 rounded-xl p-4 mb-4"
                  >
                    <div className="flex justify-between items-center mb-3">
                      <b>Medicine {index + 1}</b>
                      <button
                        aria-label="Remove medicine"
                        disabled={medicines.length === 1}
                        onClick={() =>
                          setMedicines((previous) =>
                            previous.filter(
                              (item) => item.id !== medicine.id
                            )
                          )
                        }
                        className="text-rose-600 disabled:opacity-30"
                      >
                        <Trash2 size={18} />
                      </button>
                    </div>
                    <div className="grid sm:grid-cols-2 gap-4">
                      <label className="text-sm text-slate-700">
                        Medicine Name <span className="text-rose-600">*</span>
                        <input
                          required
                          value={medicine.name}
                          onChange={(event) =>
                            update(
                              setMedicines,
                              medicine.id,
                              "name",
                              event.target.value
                            )
                          }
                          placeholder="e.g., Metformin"
                          className={inputClass}
                        />
                      </label>
                      <label className="text-sm text-slate-700">
                        Active Ingredient
                        <input
                          value={medicine.ingredient}
                          onChange={(event) =>
                            update(
                              setMedicines,
                              medicine.id,
                              "ingredient",
                              event.target.value
                            )
                          }
                          placeholder="e.g., Metformin"
                          className={inputClass}
                        />
                      </label>
                      <label className="text-sm text-slate-700">
                        Batch Number <span className="text-rose-600">*</span>
                        <input
                          required
                          value={medicine.batch}
                          onChange={(event) =>
                            update(
                              setMedicines,
                              medicine.id,
                              "batch",
                              event.target.value
                            )
                          }
                          placeholder="Enter mandatory batch number"
                          className={inputClass}
                        />
                        <span className="text-xs text-slate-500 mt-1 block">
                          Required for every medicine you add.
                        </span>
                      </label>
                      <label className="text-sm text-slate-700">
                        Manufacturer <span className="text-rose-600">*</span>
                        <input
                          required
                          value={medicine.manufacturer}
                          onChange={(event) =>
                            update(
                              setMedicines,
                              medicine.id,
                              "manufacturer",
                              event.target.value
                            )
                          }
                          placeholder="e.g., Sun Pharma"
                          className={inputClass}
                        />
                      </label>
                    </div>
                  </div>
                ))}
                <div className="mt-4 flex flex-col sm:flex-row sm:items-center gap-3">
                  <button
                    type="button"
                    onClick={saveMedicines}
                    disabled={savingMedicines}
                    className="bg-teal-700 hover:bg-teal-800 disabled:opacity-60 text-white rounded-xl px-5 py-3 font-semibold transition"
                  >
                    {savingMedicines ? "Saving medicines..." : "Save Medicines to Database"}
                  </button>
                  {saveStatus && (
                    <p role="status" className="text-sm text-slate-700">{saveStatus}</p>
                  )}
                </div>
              </section>
              <div className="flex justify-end">
                <button
                  onClick={analyze}
                  className="bg-teal-700 hover:bg-teal-800 text-white rounded-xl px-7 py-3 font-bold flex items-center gap-2 transition"
                >
                  Generate Safety Report
                  <ArrowRight size={18} />
                </button>
              </div>
            </>
          )}
          {/* Results */}
          {page === "Results" && report && (
            <>
              <div className="flex flex-wrap justify-between gap-4 mb-6">
                <div>
                  <h2 className="text-2xl font-bold">
                    Analysis Report
                  </h2>
                  <p className="text-sm text-slate-500 mt-1">
                    {report.date}
                  </p>
                </div>
                <button
                  onClick={() => window.print()}
                  className="flex gap-2 items-center border border-slate-200 rounded-xl px-4 py-2 bg-white"
                >
                  <Download size={17} />
                  Print / Save PDF
                </button>
              </div>
              <div className="rounded-xl bg-amber-50 border border-amber-200 p-4 mb-6 text-amber-900 flex gap-3">
                <AlertTriangle className="shrink-0" />
                <p className="text-sm">
                  <b>Prototype safety notice.</b> Counterfeit screening uses
                  the current trusted-batch database only. A database match
                  does not guarantee authenticity, and missing records do not
                  prove counterfeiting. Suitability and interaction coverage
                  may be incomplete. Do not use this report to make treatment
                  decisions without a qualified healthcare professional.
                </p>
              </div>
              <div className="grid sm:grid-cols-3 gap-4 mb-6">
                {[
                  ["Conditions", report.conditions.length],
                  ["Medicines", report.drugs.length],
                  [
                    "Interaction Pairs",
                    (report.drugs.length *
                      (report.drugs.length - 1)) /
                      2,
                  ],
                ].map(([label, value]) => (
                  <div key={label} className={`${card} p-5`}>
                    <p className="text-sm text-slate-500">
                      {label}
                    </p>
                    <p className="text-3xl font-bold mt-2">
                      {value}
                    </p>
                  </div>
                ))}
              </div>
              <div className={`${card} p-6 mb-5`}>
                <h3 className="font-bold text-lg mb-4">
                  1. Medicine–Illness Matching
                </h3>
                <p className="text-sm mb-4">
                  <b>Entered conditions:</b>{" "}
                  {report.conditions.join(", ")}
                </p>
                {report.checks.map((check, index) => (
                  <div key={index} className="border-t py-3">
                    <b>{check.name}</b>
                    <p className="text-sm text-slate-500 mt-1">
                      {check.indication}
                    </p>
                  </div>
                ))}
              </div>
              <div className={`${card} p-6 mb-5`}>
                <h3 className="font-bold text-lg mb-4">
                  2. Counterfeit Screening
                </h3>
                {report.drugs.map((medicine, index) => {
                  const verification = report.counterfeitResults?.[index];
                  const statusStyles = {
                    VERIFIED: "text-emerald-700 bg-emerald-50",
                    FLAGGED: "text-rose-700 bg-rose-50",
                    UNVERIFIED: "text-amber-700 bg-amber-50",
                    ERROR: "text-rose-700 bg-rose-50",
                  };

                  return (
                    <div
                      key={medicine.id || index}
                      className="border-t py-4"
                    >
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <b>{medicine.name}</b>
                        <span
                          className={`rounded-full px-3 py-1 text-xs font-bold ${
                            statusStyles[verification?.status] ||
                            "text-slate-700 bg-slate-100"
                          }`}
                        >
                          {verification?.status || "NOT_CHECKED"}
                        </span>
                      </div>
                      <p className="text-sm text-slate-500 mt-2">
                        Batch: {medicine.batch}
                      </p>
                      <p className="text-sm text-slate-500">
                        Manufacturer: {medicine.manufacturer || "Not provided"}
                      </p>
                      <p className="text-sm text-slate-700 mt-2">
                        {verification?.explanation ||
                          "No verification result is available."}
                      </p>
                      <p className="text-xs text-slate-500 mt-2 wrap-break-word">
                        Evidence source: {verification?.evidenceSource || "Not available"}
                      </p>
                    </div>
                  );
                })}
              </div>
              
              <div className={`${card} p-6 mb-5`}>
                <h3 className="font-bold text-lg mb-3">
                  3. Drug Interaction Review
                </h3>

                {(() => {
                  let interactions = report.interactions;

                  // Handle either an array or a JSON string.
                  if (typeof interactions === "string") {
                    try {
                      interactions = JSON.parse(interactions);
                    } catch {
                      interactions = [];
                    }
                  }

                  if (!Array.isArray(interactions)) {
                    interactions = [];
                  }

                  if (interactions.length === 0) {
                    return (
                      <p className="text-sm text-slate-600">
                        No interaction results are available for this analysis.
                        This does not mean the medicines are safe to combine.
                      </p>
                    );
                  }

                  return (
                    <div className="space-y-4">
                      {interactions.map((interaction, index) => {
                        const status = interaction.status || "NOT_CHECKED";

                        const isContraindicated =
                          status.toUpperCase() === "CONTRAINDICATED";

                        const isNotChecked =
                          status.toUpperCase() === "NOT_CHECKED";

                        const statusClass = isContraindicated
                          ? "bg-red-100 text-red-800 border-red-200"
                          : isNotChecked
                            ? "bg-amber-100 text-amber-800 border-amber-200"
                            : "bg-blue-100 text-blue-800 border-blue-200";

                        const firstName =
                          interaction.firstMedicineName || "Medicine A";

                        const secondName =
                          interaction.secondMedicineName || "Medicine B";

                        const source = interaction.evidenceSource || "";

                        return (
                          <div
                            key={`${interaction.firstMedicineId ?? firstName}-${interaction.secondMedicineId ?? secondName}-${index}`}
                            className="rounded-xl border border-slate-200 p-4"
                          >
                            <div className="flex flex-wrap items-start justify-between gap-3">
                              <div>
                                <p className="font-semibold text-slate-900">
                                  {firstName} <span className="text-slate-400">+</span>{" "}
                                  {secondName}
                                </p>
                                <p className="text-sm text-slate-500 mt-1">
                                  Drug interaction result
                                </p>
                              </div>

                              <span
                                className={`inline-flex rounded-full border px-3 py-1 text-xs font-bold ${statusClass}`}
                              >
                                {status.replaceAll("_", " ")}
                              </span>
                            </div>

                            <div className="mt-4">
                              <p className="text-sm font-semibold text-slate-700 mb-1">
                                Explanation
                              </p>

                              <p className="text-sm leading-6 text-slate-600">
                                {interaction.explanation ||
                                  "No verified interaction explanation is available."}
                              </p>
                            </div>

                            <div className="mt-3">
                              <p className="text-sm font-semibold text-slate-700 mb-1">
                                Evidence source
                              </p>

                              {source.startsWith("https://") ? (
                                <a
                                  href={source.slice(source.indexOf("https://"))}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className="text-sm text-blue-700 underline break-all"
                                >
                                  Open evidence source
                                </a>
                              ) : (
                                <p className="text-sm text-slate-500 break-words">
                                  {source || "No verified evidence source is available."}
                                </p>
                              )}
                            </div>

                            {isContraindicated && (
                              <div className="mt-4 rounded-lg bg-red-50 border border-red-200 p-3">
                                <p className="text-sm font-semibold text-red-800">
                                  Serious safety warning
                                </p>
                                <p className="text-sm text-red-700 mt-1">
                                  This combination is flagged as contraindicated in the
                                  available reference. Seek guidance from a licensed
                                  clinician or pharmacist.
                                </p>
                              </div>
                            )}

                            {isNotChecked && (
                              <p className="mt-3 text-sm text-amber-800">
                                This combination has not been verified. Do not interpret
                                this result as evidence that the combination is safe.
                              </p>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  );
                })()}
              </div>

              <div className={`${card} p-6`}>
                <h3 className="font-bold text-lg mb-3">
                  Safety Recommendations
                </h3>
                <p className="text-sm text-slate-600">
                  Ask a licensed pharmacist or clinician to verify
                  medicine indications, interactions, and authenticity.
                  Do not stop or change prescribed medicines based on
                  this prototype.
                </p>
              </div>
            </>
          )}
          {/* Reports */}
          {page === "Reports" && (
            <>
              <h2 className="text-2xl font-bold mb-5">
                Session Reports
              </h2>
              {history.length > 0 ? (
                history.map((savedReport) => (
                  <button
                    key={savedReport.id}
                    onClick={() => {
                      setReport(savedReport);
                      setPage("Results");
                    }}
                    className={`${card} p-5 w-full text-left mb-3 flex items-center gap-3 hover:border-teal-400 transition`}
                  >
                    <FileText className="text-teal-700 shrink-0" />
                    <div className="min-w-0">
                      <b className="wrap-break-word">
                        {savedReport.drugs
                          .map((medicine) => medicine.name)
                          .join(", ")}
                      </b>
                      <p className="text-xs text-slate-500 mt-1">
                        {savedReport.date}
                      </p>
                    </div>
                    <ArrowRight className="ml-auto shrink-0" />
                  </button>
                ))
              ) : (
                <div className={`${card} p-8 text-slate-500`}>
                  No reports yet. Create your first analysis from the
                  New Analysis page.
                  <button
                    onClick={() => setPage("New Analysis")}
                    className="block mt-4 text-teal-700 font-semibold"
                  >
                    Start New Analysis →
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </main>
    </div>
  );
}
export default App;
