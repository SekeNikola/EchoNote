# 🔄 Logion Checkbox Converters

## Overview

This utility provides seamless conversion between the Android app's JSON format and the WebUI's HTML checkbox format.

### Format Specifications

**Android App JSON Format:**
```json
{
  "text": "Main note content",
  "checkboxes": [
    {"text": "Task description", "checked": false},
    {"text": "Completed task", "checked": true}
  ]
}
```

**WebUI HTML Format:**
```html
<div class="note-text">Main note content</div>
<div class="checkbox-item">
  <input type="checkbox">
  <span>Task description</span>
</div>
<div class="checkbox-item completed">
  <input type="checkbox" checked>
  <span>Completed task</span>
</div>
```

## Functions

### `htmlToJsonCheckboxes(html)`
Converts WebUI HTML to Android app JSON format.

**Example:**
```javascript
const html = `
  <div class="note-text">Meeting notes</div>
  <div class="checkbox-item">
    <input type="checkbox" checked>
    <span>Review reports</span>
  </div>
`;

const json = htmlToJsonCheckboxes(html);
console.log(json);
// Output: {"text":"Meeting notes","checkboxes":[{"text":"Review reports","checked":true}]}
```

### `jsonToHtmlCheckboxes(jsonString)`
Converts Android app JSON to WebUI HTML format.

**Example:**
```javascript
const json = '{"text":"Project tasks","checkboxes":[{"text":"Write tests","checked":false}]}';
const html = jsonToHtmlCheckboxes(json);
console.log(html);
// Output: HTML with interactive checkboxes
```

## Integration Guide

### 🔧 WebUI Implementation

#### 1. Include the converter script
```html
<script src="checkbox-converters.js"></script>
```

#### 2. When saving note from WebUI to server
```javascript
function saveNote() {
  const htmlContent = document.getElementById('note-editor').innerHTML;
  const jsonForServer = htmlToJsonCheckboxes(htmlContent);
  
  fetch('/api/notes/123', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ snippet: jsonForServer })
  });
}
```

#### 3. When loading note from server into WebUI
```javascript
function loadNote(noteId) {
  fetch(`/api/notes/${noteId}`)
    .then(res => res.json())
    .then(note => {
      const htmlContent = jsonToHtmlCheckboxes(note.snippet);
      document.getElementById('note-display').innerHTML = htmlContent;
    });
}
```

#### 4. Handle checkbox interactions
```javascript
// This function is automatically called when checkboxes are clicked
function handleCheckboxChange(checkbox, index) {
  // Auto-save functionality
  const container = checkbox.closest('[data-note-id]');
  const noteId = container.dataset.noteId;
  
  // Get current HTML content
  const htmlContent = container.innerHTML;
  const jsonContent = htmlToJsonCheckboxes(htmlContent);
  
  // Save to server
  fetch(`/api/notes/${noteId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ snippet: jsonContent })
  });
}
```

## Testing

Open `checkbox-converter-test.html` in your browser to:
- Test both conversion directions
- Verify edge cases (empty content, special characters, malformed JSON)
- See live examples of the conversion process

## Benefits

✅ **Android app continues working with JSON** - No changes needed to Android code
✅ **WebUI gets real interactive checkboxes** - Better user experience  
✅ **Consistent data format** - No more mixed HTML/JSON in database
✅ **Bi-directional conversion** - Seamless data flow between platforms
✅ **Error handling** - Graceful fallbacks for malformed data
✅ **XSS protection** - HTML entities are properly escaped

## Error Handling

- **Malformed JSON**: Returns original text as-is
- **Missing elements**: Gracefully skips invalid checkboxes
- **XSS prevention**: All user content is HTML-escaped
- **Empty content**: Returns appropriate empty state

## File Structure

```
Logion/
├── checkbox-converters.js          # Main converter functions
├── checkbox-converter-test.html    # Testing interface
└── CHECKBOX_CONVERTERS_README.md   # This documentation
```