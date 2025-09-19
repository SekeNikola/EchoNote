/**
 * Checkbox Format Converters for EchoNote WebUI ↔ Android App
 *
 * Android App expects JSON like:
 * {
 *   "text": "optional freeform text",
 *   "checkboxes": [
 *     {"text": "task text", "checked": false}
 *   ]
 * }
 *
 * WebUI renders real HTML <input type="checkbox"> elements.
 */

/**
 * HTML → JSON
 * Converts WebUI HTML checkboxes into Android app JSON format.
 */
function htmlToJsonCheckboxes(html) {
  const container = document.createElement("div");
  container.innerHTML = html;

  const checkboxes = [];
  container.querySelectorAll(".checkbox-item").forEach(item => {
    const input = item.querySelector("input[type='checkbox']");
    const span = item.querySelector("span");
    if (input && span) {
      checkboxes.push({
        text: span.textContent.trim(),
        checked: input.checked
      });
    }
  });

  return JSON.stringify({
    text: "", // free text not tied to checkboxes
    checkboxes
  });
}

/**
 * JSON → HTML
 * Converts Android app JSON into WebUI HTML checkboxes.
 */
function jsonToHtmlCheckboxes(jsonString) {
  let obj;
  try {
    obj = JSON.parse(jsonString);
  } catch (e) {
    // If not valid JSON (legacy plain text or HTML), just return as-is
    return jsonString;
  }

  let html = "";

  // Render text
  if (obj.text && obj.text.trim()) {
    html += `<div class="note-text" style="margin-bottom: 12px; color:#fff; line-height:1.5;">
               ${escapeHtml(obj.text)}
             </div>`;
  }

  // Render checkboxes
  if (Array.isArray(obj.checkboxes)) {
    html += obj.checkboxes.map((cb, i) => {
      const id = `checkbox-${Date.now()}-${i}`;
      const checked = cb.checked ? "checked" : "";
      const textStyle = cb.checked
        ? "text-decoration:line-through; opacity:0.7; color:#888;"
        : "text-decoration:none; opacity:1; color:#fff;";

      return `
        <div class="checkbox-item ${cb.checked ? "checkbox-checked completed" : ""}" 
             style="display:flex;align-items:center;margin:6px 0;">
          <input type="checkbox" id="${id}" ${checked} 
                 onchange="handleCheckboxChange(this, ${i})"
                 style="margin-right:8px;transform:scale(1.2);accent-color:#4CAF50;">
          <span style="flex:1; ${textStyle}" onclick="document.getElementById('${id}').click()">
            ${escapeHtml(cb.text)}
          </span>
        </div>`;
    }).join("");
  }

  return html || '<div style="color:#888;font-style:italic;">No content</div>';
}

/**
 * Escapes HTML entities for safe rendering.
 */
function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

/**
 * Checkbox toggle handler
 * Updates visuals and triggers save.
 */
function handleCheckboxChange(checkbox, index) {
  const item = checkbox.closest(".checkbox-item");
  const span = item.querySelector("span");

  if (checkbox.checked) {
    item.classList.add("checkbox-checked", "completed");
    span.style.textDecoration = "line-through";
    span.style.opacity = "0.7";
    span.style.color = "#888";
  } else {
    item.classList.remove("checkbox-checked", "completed");
    span.style.textDecoration = "none";
    span.style.opacity = "1";
    span.style.color = "#fff";
  }

  // Build updated JSON and save
  const container = item.closest("#note-editor") || document.body;
  const json = htmlToJsonCheckboxes(container.innerHTML);

  // Call your existing save function
  if (typeof saveNoteCheckboxChanges === "function") {
    saveNoteCheckboxChanges(json);
  }
}

// Export (for modules or global use)
if (typeof module !== "undefined" && module.exports) {
  module.exports = { htmlToJsonCheckboxes, jsonToHtmlCheckboxes, handleCheckboxChange, escapeHtml };
}
if (typeof window !== "undefined") {
  window.htmlToJsonCheckboxes = htmlToJsonCheckboxes;
  window.jsonToHtmlCheckboxes = jsonToHtmlCheckboxes;
  window.handleCheckboxChange = handleCheckboxChange;
  window.escapeHtml = escapeHtml;
}
