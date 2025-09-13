#!/usr/bin/env python3
"""
Simple WebSocket server for EchoNote testing
"""

import asyncio
import websockets
import json
import logging
from datetime import datetime
import uuid

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# In-memory storage for testing
notes = []
tasks = []

class EchoNoteServer:
    def __init__(self):
        self.clients = set()
        
    async def register_client(self, websocket):
        """Register a new client"""
        self.clients.add(websocket)
        logger.info(f"Client connected. Total clients: {len(self.clients)}")
        
    async def unregister_client(self, websocket):
        """Unregister a client"""
        self.clients.discard(websocket)
        logger.info(f"Client disconnected. Total clients: {len(self.clients)}")
        
    async def broadcast(self, message):
        """Broadcast message to all connected clients"""
        if self.clients:
            await asyncio.gather(
                *[client.send(message) for client in self.clients],
                return_exceptions=True
            )
            
    async def handle_message(self, websocket, message):
        """Handle incoming WebSocket messages"""
        try:
            data = json.loads(message)
            message_type = data.get('type')
            
            if message_type == 'get_notes':
                await websocket.send(json.dumps({
                    'type': 'notes',
                    'data': notes
                }))
                
            elif message_type == 'get_tasks':
                await websocket.send(json.dumps({
                    'type': 'tasks',
                    'data': tasks
                }))
                
            elif message_type == 'add_note':
                note_data = data.get('data', {})
                note_data['id'] = str(uuid.uuid4())
                note_data['updatedAt'] = datetime.now().isoformat()
                notes.append(note_data)
                
                # Broadcast to all clients
                await self.broadcast(json.dumps({
                    'type': 'note_added',
                    'data': note_data
                }))
                
            elif message_type == 'update_note':
                note_data = data.get('data', {})
                note_id = note_data.get('id')
                
                # Find and update existing note
                for i, note in enumerate(notes):
                    if note.get('id') == note_id:
                        note_data['updatedAt'] = datetime.now().isoformat()
                        notes[i] = note_data
                        break
                
                # Broadcast to all clients
                await self.broadcast(json.dumps({
                    'type': 'note_updated',
                    'data': note_data
                }))
                
            elif message_type == 'add_task':
                task_data = data.get('data', {})
                task_data['id'] = str(uuid.uuid4())
                task_data['timestamp'] = datetime.now().isoformat()
                tasks.append(task_data)
                
                # Broadcast to all clients
                await self.broadcast(json.dumps({
                    'type': 'task_added',
                    'data': task_data
                }))
                
            elif message_type == 'update_task':
                task_data = data.get('data', {})
                task_id = task_data.get('id')
                
                # Find and update existing task
                for i, task in enumerate(tasks):
                    if task.get('id') == task_id:
                        task_data['timestamp'] = datetime.now().isoformat()
                        tasks[i] = task_data
                        break
                
                # Broadcast to all clients
                await self.broadcast(json.dumps({
                    'type': 'task_updated',
                    'data': task_data
                }))
                
            else:
                logger.warning(f"Unknown message type: {message_type}")
                
        except json.JSONDecodeError:
            logger.error(f"Invalid JSON received: {message}")
        except Exception as e:
            logger.error(f"Error handling message: {e}")
            
    async def handle_client(self, websocket):
        """Handle individual client connections"""
        await self.register_client(websocket)
        try:
            async for message in websocket:
                await self.handle_message(websocket, message)
        except websockets.exceptions.ConnectionClosed:
            pass
        except Exception as e:
            logger.error(f"Error with client: {e}")
        finally:
            await self.unregister_client(websocket)

def main():
    """Start the WebSocket server"""
    server = EchoNoteServer()
    
    # Add some sample data
    notes.extend([
        {
            'id': '1',
            'title': 'Welcome to EchoNote',
            'body': 'This is your first note! You can add, edit, and sync notes across devices.',
            'updatedAt': datetime.now().isoformat()
        },
        {
            'id': '2', 
            'title': 'Meeting Notes',
            'body': 'Discuss project timeline and milestones. Assign tasks to team members.',
            'updatedAt': datetime.now().isoformat()
        }
    ])
    
    tasks.extend([
        {
            'id': '1',
            'title': 'Buy groceries',
            'body': 'Milk, eggs, bread, and coffee',
            'done': False,
            'timestamp': datetime.now().isoformat()
        },
        {
            'id': '2',
            'title': 'Finish report',
            'body': 'Complete quarterly analysis report',
            'done': False,
            'timestamp': datetime.now().isoformat()
        }
    ])
    
    async def run_server():
        logger.info("Starting EchoNote WebSocket server on 0.0.0.0:8080")
        async with websockets.serve(server.handle_client, "0.0.0.0", 8080):
            await asyncio.Future()  # Run forever
    
    try:
        asyncio.run(run_server())
    except KeyboardInterrupt:
        logger.info("Server stopped")

if __name__ == "__main__":
    main()