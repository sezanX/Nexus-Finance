# Lab Report: Nexus Finance Mobile Application

**Course:** Software Development  
**Project Name:** Nexus Finance  
**Platform:** Android (Kotlin)  
**Date:** [Insert Date]  
**Student Name:** [Insert Name]  
**Student ID:** [Insert ID]  

---

## 1. Objective
The objective of this project was to develop a comprehensive, offline-first personal finance tracking application for Android. The application aims to solve the problem of managing daily expenses, income, and budgets efficiently without requiring mandatory cloud sync or internet access, ensuring user data privacy.

## 2. Introduction
Nexus Finance is a modern Android application designed for personal finance management. Many existing solutions are either too complex, require paid subscriptions, or force users to store their financial data on remote servers. Nexus Finance addresses these issues by providing a robust local SQLite database for data storage, alongside premium features like AI-assisted financial insights (via OpenRouter API), biometric privacy, and optional Google Drive backup.

## 3. Methodology & Implementation

The application follows a layered architecture utilizing standard Android components:
- **UI Layer:** XML layouts with ViewBinding, adhering to Material 3 design principles for a polished and professional look.
- **Data Layer:** SQLite database via `SQLiteOpenHelper` for persistent storage of transactions, budgets, and user preferences.
- **Business Logic:** Handled through Kotlin Coroutines for asynchronous database operations and a dedicated `FinanceAnalytics` object for complex calculations (e.g., savings rate, budget progress, cashflow trends).

### Key Features Implemented:
1. **Transaction Management:** Users can add, edit, and delete income and expense records. A recent UI enhancement includes a "swipe-to-reveal" action menu on transaction cards, allowing quick access to Edit and Delete functions.
2. **Dashboard & Analytics:** A visually appealing dashboard presents key metrics including total balance, income/expense breakdown, and savings targets. A customizable Cashflow Trend graph allows users to visualize their spending over Weekly, Monthly, Yearly, or All-Time periods.
3. **Monthly Budgets:** Users can set limits for different categories. The app visually alerts the user when they exceed their predefined budget limits.
4. **Biometric Privacy:** To ensure financial data remains secure, a Biometric Privacy feature was implemented. When enabled via Settings, the app requires fingerprint or face authentication upon launch.
5. **Smart Notifications:** An interactive notification hub alerts users about important events, such as exceeding their monthly budget, alongside general financial tips.
6. **AI Advisor:** By integrating the OpenRouter API, users can generate personalized financial reports and actionable advice based on their local spending data.
7. **Cloud Backup:** Optional Google Sign-In allows users to securely back up their local SQLite data to their private Google Drive AppData folder.

## 4. Results
The developed application successfully meets all functional and non-functional requirements. 
- **Performance:** Database queries and analytics calculations execute efficiently on the main thread using Coroutines without freezing the UI.
- **Usability:** The implementation of dynamic UI components, such as the Quick Spend Hub and colorful transaction icons, significantly improves the user experience.
- **Security:** The integration of Android's BiometricPrompt provides a robust layer of local security, ensuring only the device owner can view financial records.

*(Note: In a physical lab report submission, you can attach screenshots of the Dashboard, Settings, Notification Hub, and Transaction Dialogs here).*

## 5. Challenges and Solutions
- **Challenge:** Managing state for swipeable RecyclerView items without using external libraries.
- **Solution:** Implemented a custom state-tracking mechanism within the `TransactionAdapter` to animate the `translationX` property, allowing a smooth "small swipe" to reveal hidden action buttons.
- **Challenge:** Securing the app locally without a backend authentication server.
- **Solution:** Utilized Android's `BiometricManager` and `BiometricPrompt` APIs to lock the `MainActivity` behind device-level authentication, saving the preference in local `SharedPreferences`.

## 6. Conclusion
The development of Nexus Finance demonstrated the practical application of Android development concepts, including UI/UX design, local database management, coroutines, and hardware API integration (biometrics). The resulting application is a fully functional, privacy-respecting financial tool ready for daily use.

---
*End of Report*
