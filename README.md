<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nexus Finance - Documentation</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f9f9f9;
        }
        .container {
            background-color: #ffffff;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        h1 {
            border-bottom: 2px solid #eaeaea;
            padding-bottom: 10px;
            color: #2c3e50;
        }
        h2 {
            margin-top: 30px;
            color: #34495e;
            border-bottom: 1px solid #eaeaea;
            padding-bottom: 5px;
        }
        ul, ol {
            padding-left: 20px;
        }
        li {
            margin-bottom: 8px;
        }
        code {
            font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, Courier, monospace;
            background-color: #f6f8fa;
            padding: 2px 5px;
            border-radius: 3px;
            font-size: 0.9em;
            color: #d63384;
        }
        pre {
            background-color: #1e1e1e;
            color: #d4d4d4;
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
            font-size: 0.9em;
        }
        pre code {
            background-color: transparent;
            color: inherit;
            padding: 0;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: center;
        }
        th {
            background-color: #f2f2f2;
        }
        .footer {
            margin-top: 40px;
            text-align: center;
            font-size: 0.9em;
            color: #777;
        }
        .emoji {
            font-style: normal;
        }
    </style>
</head>
<body>

<div class="container">
    <h1>Nexus Finance <span class="emoji">💰</span></h1>

    <p><strong>Nexus Finance</strong> is a smart, minimalist Android application designed to help users track their income and expenses effortlessly. It features a clean UI and integrates <strong>AI-powered financial analysis</strong> (via DeepSeek V3) to provide personalized insights, spending leak detection, and actionable savings plans.</p>

    <h2><span class="emoji">✨</span> Features</h2>
    <ul>
        <li><strong>Dashboard Overview</strong>: Real-time view of Total Balance, Income, and Expenses.</li>
        <li><strong>Transaction Management</strong>: Add, edit, and delete income or expense records.</li>
        <li><strong>Smart Filtering</strong>: View transactions by Today, Week, Month, Year, or All time.</li>
        <li><strong>AI Financial Auditor</strong>:
            <ul>
                <li>Uses <strong>DeepSeek V3 (via OpenRouter)</strong> to analyze your transaction history.</li>
                <li>Generates an <strong>Executive Summary</strong> of your financial health.</li>
                <li>Identifies <strong>Spending Leaks</strong> (categories draining your money).</li>
                <li>Creates a personalized <strong>Action Plan</strong> to improve savings.</li>
            </ul>
        </li>
        <li><strong>Privacy First</strong>: All transaction data is stored locally on the device using SQLite.</li>
        <li><strong>Currency Support</strong>: Multi-currency toggling (BDT, USD, Euro).</li>
    </ul>

    <h2><span class="emoji">🛠</span> Tech Stack</h2>
    <ul>
        <li><strong>Language</strong>: Kotlin</li>
        <li><strong>UI</strong>: XML Layouts (ConstraintLayout, CoordinatorLayout, Material Design Components)</li>
        <li><strong>Database</strong>: SQLite (Local Storage)</li>
        <li><strong>AI Integration</strong>: OpenRouter API (DeepSeek V3 Model)</li>
        <li><strong>Networking</strong>: Native <code>HttpURLConnection</code> (No heavy third-party networking libraries)</li>
    </ul>

    <h2><span class="emoji">🚀</span> Setup & Installation</h2>
    <ol>
        <li><strong>Clone the Repository</strong>
            <pre><code>git clone https://github.com/yourusername/nexus-finance.git</code></pre>
        </li>
        <li><strong>Open in Android Studio</strong>
            <ul>
                <li>Open Android Studio.</li>
                <li>Select <strong>File > Open</strong> and navigate to the cloned project folder.</li>
                <li>Let Gradle sync the project dependencies.</li>
            </ul>
        </li>
        <li><strong>Configure API Key</strong>
            <ul>
                <li>Get your free/paid API Key from <a href="https://openrouter.ai/" target="_blank">OpenRouter</a>.</li>
                <li>Open <code>MainActivity.kt</code>.</li>
                <li>Find the <code>API_KEY</code> variable at the top of the class:
                    <pre><code>private val API_KEY = "sk-or-v1-your-key-here"</code></pre>
                </li>
                <li>Replace the placeholder with your actual key.</li>
            </ul>
        </li>
        <li><strong>Run the App</strong>
            <ul>
                <li>Connect your Android device or start an Emulator.</li>
                <li>Click the <strong>Run</strong> button (Green Play Icon).</li>
            </ul>
        </li>
    </ol>

    <h2><span class="emoji">📸</span> Screenshots</h2>
    <table>
        <thead>
            <tr>
                <th>Dashboard</th>
                <th>Add Transaction</th>
                <th>AI Report</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td><em>(Place screenshot here)</em></td>
                <td><em>(Place screenshot here)</em></td>
                <td><em>(Place screenshot here)</em></td>
            </tr>
        </tbody>
    </table>

    <h2><span class="emoji">🤖</span> AI Configuration</h2>
    <p>The app is currently configured to use <strong>DeepSeek V3</strong> via OpenRouter. You can change the model in <code>MainActivity.kt</code>:</p>
    <pre><code>private val MODEL_NAME = "nex-agi/deepseek-v3.1-nex-n1:free"</code></pre>
    <p>To switch back to Google Gemini or another provider, simply update the <code>callOpenRouterAPI</code> function or the model string accordingly.</p>

    <h2><span class="emoji">🤝</span> Contributing</h2>
    <p>Contributions are welcome!</p>
    <ol>
        <li>Fork the project.</li>
        <li>Create your Feature Branch (<code>git checkout -b feature/AmazingFeature</code>).</li>
        <li>Commit your changes (<code>git commit -m 'Add some AmazingFeature'</code>).</li>
        <li>Push to the Branch (<code>git push origin feature/AmazingFeature</code>).</li>
        <li>Open a Pull Request.</li>
    </ol>

    <h2><span class="emoji">📄</span> License</h2>
    <p>Distributed under the MIT License. See <code>LICENSE</code> for more information.</p>

    <div class="footer">
        <p><em>Built with <span class="emoji">❤️</span> by Sezan</em></p>
    </div>
</div>

</body>
</html>
