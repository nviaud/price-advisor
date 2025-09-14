import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import Home from './features/home/Home'
import BaseLayout from './layouts/BaseLayout';
import ProductsPage from './pages/ProductsPage';
import QuotationsPage from './pages/QuotationsPage';

function App() {
  return (
    <BrowserRouter>
      <BaseLayout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/quotations" element={<QuotationsPage />} />
        </Routes>
      </BaseLayout>
    </BrowserRouter>
  );
}

export default App
