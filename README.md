# Nexus Finance 💰

**Nexus Finance** is a smart, minimalist Android application designed to help users track their income and expenses effortlessly. It features a clean UI and integrates **AI-powered financial analysis** (via DeepSeek V3) to provide personalized insights, spending leak detection, and actionable savings plans.

## ✨ Features

- **Dashboard Overview**: Real-time view of Total Balance, Income, and Expenses.
- **Transaction Management**: Add, edit, and delete income or expense records.
- **Smart Filtering**: View transactions by Today, Week, Month, Year, or All time.
- **AI Financial Auditor**:
  - Uses **DeepSeek V3 (via OpenRouter)** to analyze your transaction history.
  - Generates an **Executive Summary** of your financial health.
  - Identifies **Spending Leaks** (categories draining your money).
  - Creates a personalized **Action Plan** to improve savings.
- **Privacy First**: All transaction data is stored locally on the device using SQLite.
- **Currency Support**: Multi-currency toggling (BDT, USD, Euro).

## 🛠 Tech Stack

- **Language**: Kotlin  
- **UI**: XML Layouts (ConstraintLayout, CoordinatorLayout, Material Design Components)  
- **Database**: SQLite (Local Storage)  
- **AI Integration**: OpenRouter API (DeepSeek V3 Model)  
- **Networking**: Native `HttpURLConnection` (No heavy third-party networking libraries)  

## 🚀 Setup & Installation

1. **Clone the Repository**  
   ```bash
   git clone https://github.com/yourusername/nexus-finance.git
   ```

2. **Open in Android Studio**  
   - Open Android Studio.  
   - Select **File > Open** and navigate to the cloned project folder.  
   - Let Gradle sync the project dependencies.

3. **Configure API Key**  
   - Get your free/paid API Key from [OpenRouter](https://openrouter.ai/).  
   - Open `MainActivity.kt`.  
   - Find the `API_KEY` variable at the top of the class:
     ```kotlin
     private val API_KEY = "sk-or-v1-your-key-here"
     ```
   - Replace the placeholder with your actual key.

4. **Run the App**  
   - Connect your Android device or start an Emulator.  
   - Click the **Run** button (Green Play Icon).

## 📸 Screenshots

| Dashboard | Add Transaction | AI Report |
|-----------|------------------|-----------|
| *(Place screenshot here)* | *(Place screenshot here)* | *(Place screenshot here)* |

## 🤖 AI Configuration

The app is currently configured to use **DeepSeek V3** via OpenRouter. You can change the model in `MainActivity.kt`:

```kotlin
private val MODEL_NAME = "nex-agi/deepseek-v3.1-nex-n1:free"
```

To switch back to Google Gemini or another provider, simply update the `callOpenRouterAPI` function or the model string accordingly.

## 🤝 Contributing

Contributions are welcome!

1. Fork the project.  
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).  
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).  
4. Push to the Branch (`git push origin feature/AmazingFeature`).  
5. Open a Pull Request.

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

*Built with ❤️ by Sezan*
