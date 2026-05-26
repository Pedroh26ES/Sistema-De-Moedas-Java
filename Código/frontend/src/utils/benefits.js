export function parseBenefitDescription(description = "") {
  const parts = description
    .split("|")
    .map((part) => part.trim())
    .filter(Boolean);
  if (parts.length < 2) {
    return [];
  }
  return parts.map((part) => {
    const separator = part.indexOf(":");
    if (separator === -1) {
      return { type: "description", label: "", text: part };
    }
    const label = part.slice(0, separator).trim();
    const text = part.slice(separator + 1).trim();
    if (label.toLowerCase() === "limite") {
      return null;
    }
    if (label.toLowerCase() === "valor pratico") {
      return { type: "description", label: "", text };
    }
    return {
      type: "detail",
      label,
      text
    };
  }).filter((item) => item?.text);
}
