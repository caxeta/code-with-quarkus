import json
import sys

def validate():
    try:
        with open('todo_report.json', 'r', encoding='utf-8') as f:
            data = json.load(f)
    except Exception as e:
        print(f"Error loading JSON: {e}")
        sys.exit(1)

    if not isinstance(data, list):
        print("JSON must be a list of objects")
        sys.exit(1)

    for item in data:
        if not isinstance(item, dict):
            print("Each item in the JSON list must be a dictionary")
            sys.exit(1)

    print("Validation successful")

if __name__ == "__main__":
    validate()
