package com.example.templates

import com.example.syntax.SupportedLanguage
import com.example.viewmodel.PolyFile

data class ProjectTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val iconEmoji: String,
    val primaryLanguage: SupportedLanguage,
    val files: List<PolyFile>
)

object ProjectTemplateCatalog {

    val allTemplates: List<ProjectTemplate> = listOf(
        ProjectTemplate(
            id = "react_starter",
            title = "React 18 Component Suite",
            description = "Interactive React app with useState hooks, component cards, and live JSX rendering.",
            category = "Frontend",
            iconEmoji = "⚛",
            primaryLanguage = SupportedLanguage.REACT_JSX,
            files = listOf(
                PolyFile(
                    id = "app_jsx",
                    name = "App.jsx",
                    language = SupportedLanguage.REACT_JSX,
                    content = """
// PolyCode React 18 Boilerplate
function App() {
  const [tasks, setTasks] = React.useState([
    { id: 1, text: "Setup PolyCode Workspace", done: true },
    { id: 2, text: "Test Chrome Split-Pane Layout", done: true },
    { id: 3, text: "Query BaradAi with Google Gemini", done: false }
  ]);
  const [inputVal, setInputVal] = React.useState("");

  const addTask = () => {
    if (!inputVal.trim()) return;
    setTasks([...tasks, { id: Date.now(), text: inputVal.trim(), done: false }]);
    setInputVal("");
  };

  const toggleTask = (id) => {
    setTasks(tasks.map(t => t.id === id ? { ...t, done: !t.done } : t));
  };

  return (
    <div className="card">
      <h2 style={{color: '#00e5ff'}}>⚡ React Developer App</h2>
      <p>Powered by PolyCode by Vansh & BaradAi</p>
      
      <div style={{display: 'flex', gap: '8px', margin: '16px 0'}}>
        <input 
          value={inputVal}
          onChange={(e) => setInputVal(e.target.value)}
          placeholder="New developer goal..."
          style={{padding: '8px 12px', background: '#0f1117', color: '#fff', border: '1px solid #2a2e3d', borderRadius: '6px', flex: 1}}
        />
        <button onClick={addTask}>Add</button>
      </div>

      <ul style={{listStyle: 'none', padding: 0}}>
        {tasks.map(t => (
          <li key={t.id} onClick={() => toggleTask(t.id)} style={{
            padding: '8px 12px',
            margin: '6px 0',
            background: t.done ? '#162b20' : '#1a1d27',
            border: '1px solid #2a2e3d',
            borderRadius: '6px',
            cursor: 'pointer',
            textDecoration: t.done ? 'line-through' : 'none'
          }}>
            {t.done ? "✔" : "○"} {t.text}
          </li>
        ))}
      </ul>
    </div>
  );
}
""".trimIndent()
                ),
                PolyFile(
                    id = "styles_css",
                    name = "styles.css",
                    language = SupportedLanguage.CSS,
                    content = """
body {
  margin: 0;
  padding: 16px;
  background-color: #0f1117;
  color: #f1f5f9;
  font-family: -apple-system, sans-serif;
}

.card {
  background: #1a1d27;
  border: 1px solid #2a2e3d;
  border-radius: 12px;
  padding: 20px;
}
""".trimIndent()
                )
            )
        ),
        ProjectTemplate(
            id = "node_starter",
            title = "Node.js REST API Server",
            description = "Lightweight backend microservice with JSON route handlers and performance telemetry.",
            category = "Backend",
            iconEmoji = "🚀",
            primaryLanguage = SupportedLanguage.NODEJS,
            files = listOf(
                PolyFile(
                    id = "server_js",
                    name = "server.js",
                    language = SupportedLanguage.NODEJS,
                    content = """
// PolyCode Node.js Microservice
console.log("⚡ Initializing Node.js Express Server...");

const routes = [
  { path: "/health", method: "GET", response: { status: "OK", uptime: "99.9%" } },
  { path: "/users", method: "GET", response: { users: ["Vansh", "Maya", "Alex"] } },
  { path: "/ai/query", method: "POST", response: { engine: "BaradAi / Gemini" } }
];

routes.forEach(r => {
  console.log("[ROUTER] Registered endpoint: " + r.method + " " + r.path);
});

const heapMb = (Math.random() * 5 + 22).toFixed(1);
console.log("Process memory: " + heapMb + " MB used");
console.log("Server listening at http://localhost:8080");
""".trimIndent()
                )
            )
        ),
        ProjectTemplate(
            id = "python_starter",
            title = "Python Data & Algorithms",
            description = "Clean Python script with list comprehensions, statistics, and mathematical algorithms.",
            category = "Algorithms",
            iconEmoji = "🐍",
            primaryLanguage = SupportedLanguage.PYTHON,
            files = listOf(
                PolyFile(
                    id = "main_py",
                    name = "main.py",
                    language = SupportedLanguage.PYTHON,
                    content = """
# PolyCode Python Data Science & Algorithm Sandbox
import math

def analyze_dataset(data):
    total = sum(data)
    mean = total / len(data)
    variance = sum((x - mean) ** 2 for x in data) / len(data)
    std_dev = math.sqrt(variance)
    return {
        "count": len(data),
        "mean": round(mean, 2),
        "std_dev": round(std_dev, 2),
        "min": min(data),
        "max": max(data)
    }

metrics = [42, 65, 88, 92, 74, 95, 83, 79, 91]
print("--- PolyCode Python Analysis ---")
results = analyze_dataset(metrics)
for key, value in results.items():
    print(f"• {key.upper()}: {value}")

print("Algorithm Benchmark: Completed Successfully!")
""".trimIndent()
                )
            )
        ),
        ProjectTemplate(
            id = "java_starter",
            title = "Java 21 OOP Banking System",
            description = "Enterprise Java application with classes, encapsulation, loops, and stream processing.",
            category = "Enterprise",
            iconEmoji = "☕",
            primaryLanguage = SupportedLanguage.JAVA,
            files = listOf(
                PolyFile(
                    id = "main_java",
                    name = "Main.java",
                    language = SupportedLanguage.JAVA,
                    content = """
// PolyCode Java OOP Banking Application
public class Main {

    static class BankAccount {
        private String owner;
        private double balance;

        public BankAccount(String owner, double balance) {
            this.owner = owner;
            this.balance = balance;
        }

        public void deposit(double amount) {
            this.balance += amount;
            System.out.println("[TRANSACTION] Deposited $" + amount + " to " + owner + ". New Balance: $" + balance);
        }

        public double getBalance() {
            return balance;
        }
    }

    public static void main(String[] args) {
        System.out.println("⚡ PolyCode Java 21 JVM Initialized");
        
        BankAccount account = new BankAccount("Vansh Barad", 1250.0);
        account.deposit(500.0);
        account.deposit(250.0);

        System.out.println("Final Account Balance: $" + account.getBalance());
        System.out.println("Java Sandbox Status: OK (0 errors)");
    }
}
""".trimIndent()
                )
            )
        )
    )
}
