# PMES Frontend - Next.js Energy Planning Interface

A modern, responsive frontend for the PMES Energy Planning System built with Next.js 14, TypeScript, and Tailwind CSS.

## 🚀 Features

### Core Functionality
- **Modern Dashboard**: Real-time system status and key metrics
- **Scenario Management**: Create, edit, and manage energy planning scenarios
- **Forecasting Interface**: Run neural network-based energy demand forecasts
- **Emissions Analysis**: Comprehensive emissions calculation and visualization
- **Data Import**: Excel file upload with validation and progress tracking
- **Analytics**: Interactive charts and data visualization

### Technical Features
- **TypeScript**: Full type safety and better developer experience
- **Responsive Design**: Mobile-first approach with Tailwind CSS
- **Real-time Updates**: React Query for efficient data fetching
- **Interactive Charts**: Recharts for beautiful data visualization
- **Form Handling**: React Hook Form with validation
- **Toast Notifications**: User-friendly feedback system

## 🛠️ Technology Stack

- **Framework**: Next.js 14 with App Router
- **Language**: TypeScript
- **Styling**: Tailwind CSS with custom components
- **State Management**: React Query + Zustand
- **Charts**: Recharts
- **Forms**: React Hook Form
- **Icons**: Heroicons
- **Animations**: Framer Motion
- **Notifications**: React Hot Toast

## 📋 Prerequisites

- Node.js 18+ 
- npm or yarn
- Backend API running on `http://localhost:8080`

## 🚀 Quick Start

### 1. Install Dependencies
```bash
cd frontend
npm install
```

### 2. Start Development Server
```bash
npm run dev
```

### 3. Open in Browser
Navigate to `http://localhost:3000`

## 📁 Project Structure

```
frontend/
├── src/
│   ├── app/                 # Next.js App Router pages
│   │   ├── globals.css     # Global styles
│   │   ├── layout.tsx      # Root layout
│   │   └── page.tsx        # Dashboard page
│   ├── components/         # Reusable components
│   │   ├── Layout/        # Layout components
│   │   ├── Dashboard/     # Dashboard components
│   │   ├── Scenarios/     # Scenario management
│   │   ├── Forecasting/   # Forecasting interface
│   │   ├── Emissions/     # Emissions analysis
│   │   ├── Import/        # Data import
│   │   └── UI/           # UI components
│   ├── lib/              # Utilities and API
│   │   ├── api.ts        # API client
│   │   └── utils.ts      # Utility functions
│   ├── types/            # TypeScript types
│   │   └── index.ts      # Type definitions
│   └── hooks/            # Custom React hooks
├── public/               # Static assets
├── package.json          # Dependencies
├── tailwind.config.js    # Tailwind configuration
├── tsconfig.json         # TypeScript configuration
└── next.config.js        # Next.js configuration
```

## 🎨 Design System

### Colors
- **Primary**: Blue (#3b82f6) - Main actions and branding
- **Success**: Green (#10b981) - Positive states
- **Warning**: Orange (#f59e0b) - Caution states
- **Error**: Red (#ef4444) - Error states
- **Secondary**: Gray (#64748b) - Secondary text and borders

### Components
- **Cards**: Consistent card layouts with headers and content
- **Buttons**: Primary, secondary, outline, and danger variants
- **Forms**: Input fields, selects, and textareas
- **Tables**: Sortable data tables with pagination
- **Charts**: Line, bar, and pie charts for data visualization

## 📊 Key Pages

### Dashboard (`/`)
- System overview and key metrics
- Real-time charts and visualizations
- Quick actions and recent scenarios
- System health monitoring

### Scenarios (`/scenarios`)
- List all energy planning scenarios
- Create new scenarios with forms
- Edit existing scenarios
- Scenario comparison tools

### Forecasting (`/forecasting`)
- Run baseline energy demand forecasts
- Scenario-based forecasting
- Neural network configuration
- Forecast results visualization

### Emissions (`/emissions`)
- CO2 emissions calculations
- Sector and energy source breakdowns
- Yearly emissions tracking
- Scenario comparison analysis

### Data Import (`/import`)
- Excel file upload interface
- Data validation and error handling
- Import progress tracking
- Data preview and confirmation

### Analytics (`/analytics`)
- Advanced data visualization
- Trend analysis and insights
- Custom chart generation
- Export capabilities

## 🔧 Configuration

### Environment Variables
```bash
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080

# Development
NODE_ENV=development
```

### API Proxy
The frontend automatically proxies API requests to the backend:
```javascript
// next.config.js
async rewrites() {
  return [
    {
      source: '/api/:path*',
      destination: 'http://localhost:8080/api/:path*',
    },
  ];
}
```

## 🧪 Testing

### Run Tests
```bash
npm test
```

### Type Checking
```bash
npm run type-check
```

### Linting
```bash
npm run lint
```

## 📦 Build & Deployment

### Build for Production
```bash
npm run build
```

### Start Production Server
```bash
npm start
```

### Docker Deployment
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

## 🔌 API Integration

### API Client
The frontend uses a centralized API client (`src/lib/api.ts`) with:
- Axios for HTTP requests
- Request/response interceptors
- Error handling
- TypeScript support

### Data Fetching
React Query is used for:
- Caching and state management
- Background refetching
- Optimistic updates
- Error handling

## 🎯 Key Components

### Layout Components
- **Sidebar**: Navigation with responsive design
- **Header**: Page titles and actions
- **Footer**: System information

### Dashboard Components
- **DashboardChart**: Interactive charts and visualizations
- **QuickActions**: Common task shortcuts
- **RecentScenarios**: Latest scenario overview

### Form Components
- **ScenarioForm**: Create and edit scenarios
- **ForecastForm**: Configure forecasting parameters
- **ImportForm**: File upload with validation

### Data Components
- **DataTable**: Sortable and filterable tables
- **ChartContainer**: Responsive chart wrappers
- **StatusIndicator**: System health indicators

## 🚀 Performance

### Optimizations
- **Code Splitting**: Automatic route-based splitting
- **Image Optimization**: Next.js Image component
- **Bundle Analysis**: Webpack bundle analyzer
- **Caching**: React Query caching strategies

### Monitoring
- **Error Tracking**: Error boundaries and logging
- **Performance**: Core Web Vitals monitoring
- **Analytics**: User interaction tracking

## 🔒 Security

### Best Practices
- **Input Validation**: Client-side validation
- **XSS Prevention**: Sanitized content rendering
- **CSRF Protection**: API request validation
- **Content Security**: CSP headers

## 🤝 Contributing

### Development Workflow
1. Create feature branch
2. Implement changes with TypeScript
3. Add tests for new functionality
4. Update documentation
5. Submit pull request

### Code Style
- **TypeScript**: Strict mode enabled
- **ESLint**: Consistent code formatting
- **Prettier**: Automatic code formatting
- **Conventional Commits**: Standard commit messages

## 📚 Documentation

### API Documentation
- Swagger UI available at `/swagger-ui.html`
- TypeScript types for all API responses
- JSDoc comments for complex functions

### Component Documentation
- Storybook integration for component development
- Props documentation with TypeScript
- Usage examples and best practices

## 🆘 Support

### Common Issues
- **API Connection**: Check backend server status
- **Build Errors**: Verify TypeScript types
- **Styling Issues**: Check Tailwind configuration

### Getting Help
- Check the backend API documentation
- Review TypeScript error messages
- Consult the component library

## 🔄 Version History

- **v1.0.0**: Initial release with core functionality
- Dashboard with real-time metrics
- Scenario management interface
- Forecasting and emissions analysis
- Data import capabilities
- Responsive design with Tailwind CSS 