This project uses React and TypeScript for the frontend development.
The frontend is built using Vite for fast build and development cycles.

The goal of the project is to provide a frontend where user can upload quotation files (PDF, PNG, ...). The quotation is parsed and data are extracted by the backend. By providing a lot of quotation, it's possible to have some analytics on products (analytics are managed by the backend). Then the user can research products in order to see if the price is fair or not.

The structure of the projects is as follows:
```frontend/
├── public/                 # Static assets
├── src/                    # Source files
│   ├── assets/            # Images, fonts, and other static resources
│   ├── components/        # Reusable React components
│   ├── pages/             # Page components for different routes
│   ├── services/          # API service calls
│   ├── hooks/             # Custom React hooks
│   ├── context/           # Context API for state management
│   ├── styles/            # Global and component-specific styles
│   ├── App.tsx            # Main application component
│   ├── main.tsx           # Entry point for the React application
│   └── vite-env.d.ts      # Vite environment type definitions
├── .eslintrc.js            # ESLint configuration
├── .prettierrc             # Prettier configuration
├── tsconfig.json           # TypeScript configuration
├── vite.config.ts          # Vite configuration
└── package.json            # Project metadata and dependencies
```

Please follow these guidelines when writing code for the frontend:
- Use TypeScript and React best practices.
- Prefer functional components over class components.
- Use Vite for build and development.
- Follow the existing project structure and naming conventions.
- In HTML, never use inline styles; prefer using Tailwind CSS or styled-components.
- Ensure accessibility standards are met (e.g., ARIA roles, keyboard navigation).
- Optimize performance by avoiding unnecessary re-renders and using React.memo where appropriate.
- Use state management libraries (like Redux or Context API) as needed, but avoid overcomplicating state.
- Follow the DRY (Don't Repeat Yourself) principle to avoid code duplication.
- Use ESLint and Prettier for code formatting and linting.
- Ensure proper error handling and user feedback in the UI.
- Use environment variables for configuration settings, avoiding hard-coded values.
- Maintain a clean and organized project structure for scalability and maintainability.
- Follow security best practices to protect against common vulnerabilities (e.g., XSS, CSRF).
- Stay updated with the latest trends and updates in the React and TypeScript ecosystems.
