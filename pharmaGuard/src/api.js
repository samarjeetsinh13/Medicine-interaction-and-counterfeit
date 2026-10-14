
const API_BASE_URL = "http://localhost:8080/api";

async function request(endpoint, options = {}) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      ...(options.body
        ? { "Content-Type": "application/json" }
        : {}),
      ...options.headers,
    },
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(
      message || `API request failed: ${response.status}`
    );
  }

  const contentType = response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    return response.json();
  }

  return response.text();
}

export const api = {
  health: () => request("/health"),

  getMedicines: () => request("/medicines"),

  getIllnesses: () => request("/illnesses"),

  createMedicines: (medicines) =>
    request("/medicines", {
      method: "POST",
      body: JSON.stringify({ medicines }),
    }),

  createIllnesses: (illnesses) =>
    request("/illnesses", {
      method: "POST",
      body: JSON.stringify({ illnesses }),
    }),

  generateAnalysis: (medicineIds, illnessIds) =>
    request("/analysis", {
      method: "POST",
      body: JSON.stringify({
        medicineIds,
        illnessIds,
      }),
    }),

  verifyCounterfeit: (medicineName, batchNumber, manufacturer) =>
    request("/counterfeit/verify", {
      method: "POST",
      body: JSON.stringify({
        medicineName,
        batchNumber,
        manufacturer,
      }),
    }),
};
