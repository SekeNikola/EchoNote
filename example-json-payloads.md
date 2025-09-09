# Example JSON Payloads

## Task Examples

### GET /tasks Response
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Buy groceries",
    "done": false,
    "updatedAt": "2025-01-09T10:30:00Z"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Complete project report",
    "done": true,
    "updatedAt": "2025-01-09T09:15:00Z"
  }
]
```

### POST /tasks Request
```json
{
  "title": "Call dentist",
  "done": false
}
```

### POST /tasks Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "title": "Call dentist",
  "done": false,
  "updatedAt": "2025-01-09T11:45:00Z"
}
```

### PUT /tasks/{id} Request
```json
{
  "title": "Call dentist for appointment",
  "done": true
}
```

### PUT /tasks/{id} Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "title": "Call dentist for appointment",
  "done": true,
  "updatedAt": "2025-01-09T12:00:00Z"
}
```

## Note Examples

### GET /notes Response
```json
[
  {
    "id": "660f9500-f30c-52e5-b827-557766551111",
    "title": "Meeting Notes",
    "body": "Discussion about project timeline and deliverables. Next meeting scheduled for Friday.",
    "updatedAt": "2025-01-09T14:20:00Z"
  },
  {
    "id": "660f9500-f30c-52e5-b827-557766551112",
    "title": "Recipe Ideas",
    "body": "- Pasta with garlic and olive oil\n- Grilled chicken salad\n- Vegetable stir-fry",
    "updatedAt": "2025-01-09T13:10:00Z"
  }
]
```

### POST /notes Request
```json
{
  "title": "Shopping List",
  "body": "- Milk\n- Bread\n- Eggs\n- Apples\n- Cheese"
}
```

### POST /notes Response
```json
{
  "id": "660f9500-f30c-52e5-b827-557766551113",
  "title": "Shopping List",
  "body": "- Milk\n- Bread\n- Eggs\n- Apples\n- Cheese",
  "updatedAt": "2025-01-09T15:30:00Z"
}
```

### PUT /notes/{id} Request
```json
{
  "title": "Updated Shopping List",
  "body": "- Milk\n- Bread\n- Eggs\n- Apples\n- Cheese\n- Butter\n- Yogurt"
}
```

### PUT /notes/{id} Response
```json
{
  "id": "660f9500-f30c-52e5-b827-557766551113",
  "title": "Updated Shopping List",
  "body": "- Milk\n- Bread\n- Eggs\n- Apples\n- Cheese\n- Butter\n- Yogurt",
  "updatedAt": "2025-01-09T15:45:00Z"
}
```

## WebSocket Sync Messages

### Task Added
```json
{
  "type": "task_added",
  "data": "{\"id\":\"550e8400-e29b-41d4-a716-446655440003\",\"title\":\"New task\",\"done\":false,\"updatedAt\":\"2025-01-09T16:00:00Z\"}"
}
```

### Task Updated
```json
{
  "type": "task_updated",
  "data": "{\"id\":\"550e8400-e29b-41d4-a716-446655440003\",\"title\":\"Updated task\",\"done\":true,\"updatedAt\":\"2025-01-09T16:05:00Z\"}"
}
```

### Note Added
```json
{
  "type": "note_added",
  "data": "{\"id\":\"660f9500-f30c-52e5-b827-557766551114\",\"title\":\"New note\",\"body\":\"Note content here\",\"updatedAt\":\"2025-01-09T16:10:00Z\"}"
}
```

### Note Updated
```json
{
  "type": "note_updated",
  "data": "{\"id\":\"660f9500-f30c-52e5-b827-557766551114\",\"title\":\"Updated note\",\"body\":\"Updated content\",\"updatedAt\":\"2025-01-09T16:15:00Z\"}"
}
```
