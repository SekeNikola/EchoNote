#!/usr/bin/env python3
"""
EchoNote Server Test Script
Tests if the Android server is accessible from the network
"""

import requests
import json
import sys
import time

def test_server(base_url):
    """Test the EchoNote server endpoints"""
    print(f"Testing server at: {base_url}")
    print("-" * 50)
    
    # Test basic connectivity
    try:
        response = requests.get(f"{base_url}/tasks", timeout=5)
        if response.status_code == 200:
            print("✅ Server is responding!")
            tasks = response.json()
            print(f"   Found {len(tasks)} tasks")
        else:
            print(f"❌ Server responded with error: {response.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to server")
        print("   Possible issues:")
        print("   - Server not running on Android device")
        print("   - Wrong IP address or port")
        print("   - Not on same WiFi network") 
        return False
    except requests.exceptions.Timeout:
        print("❌ Connection timeout")
        return False
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False
    
    # Test all endpoints
    endpoints = [
        ("GET", "/tasks", "Get Tasks"),
        ("GET", "/notes", "Get Notes"),
        ("POST", "/tasks", "Create Task", {"title": "Test Task", "done": False}),
        ("POST", "/notes", "Create Note", {"title": "Test Note", "body": "Test content"})
    ]
    
    print("\nTesting endpoints:")
    for method, path, name, *body in endpoints:
        try:
            if method == "GET":
                response = requests.get(f"{base_url}{path}", timeout=5)
            else:
                response = requests.post(
                    f"{base_url}{path}", 
                    json=body[0] if body else None,
                    timeout=5
                )
            
            if response.status_code in [200, 201]:
                print(f"✅ {name}: OK")
            else:
                print(f"❌ {name}: {response.status_code}")
        except Exception as e:
            print(f"❌ {name}: {e}")
        
        time.sleep(0.5)  # Small delay between requests
    
    return True

def scan_network():
    """Try to find the server on common IP addresses"""
    print("Scanning common IP addresses...")
    
    # Common private network ranges
    ip_ranges = [
        "192.168.1.{}",
        "192.168.0.{}",
        "10.0.0.{}",
        "172.16.0.{}"
    ]
    
    for ip_range in ip_ranges:
        for i in [100, 101, 102, 103, 104, 105, 110, 120, 150]:
            ip = ip_range.format(i)
            url = f"http://{ip}:8080"
            
            try:
                response = requests.get(f"{url}/tasks", timeout=2)
                if response.status_code == 200:
                    print(f"🎉 Found server at: {url}")
                    return url
            except:
                pass  # Continue scanning
            
            print(f"   Trying {ip}...", end="\r")
    
    print("\n❌ Server not found on common IP addresses")
    return None

if __name__ == "__main__":
    if len(sys.argv) > 1:
        server_url = sys.argv[1]
        if not server_url.startswith("http"):
            server_url = f"http://{server_url}:8080"
    else:
        print("No server URL provided. Scanning network...")
        server_url = scan_network()
        if not server_url:
            print("\nUsage: python server_test.py <server_ip>")
            print("Example: python server_test.py 192.168.1.100")
            sys.exit(1)
    
    success = test_server(server_url)
    
    if success:
        print(f"\n🎉 Server test completed successfully!")
        print(f"   Use this URL in the web client: {server_url}")
    else:
        print(f"\n❌ Server test failed")
        print("   Check that:")
        print("   1. Android app is running")
        print("   2. Both devices are on same WiFi")
        print("   3. Server Status shows correct IP in Android app Settings")
