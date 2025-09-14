import React from 'react';
import { Link } from 'react-router-dom';

const Header: React.FC = () => (
  <header className="bg-gradient-to-r from-teal-500 via-blue-600 to-indigo-700 text-white shadow-md">
    <div className="container mx-auto flex items-center justify-between py-4 px-6">
      <div className="flex items-center gap-2">
        <img src="/price-advisor-logo.svg" alt="Logo" className="h-8 w-8" />
        <Link to="/" className="font-bold text-xl tracking-wide hover:text-yellow-300 transition">Price Advisor</Link>
      </div>
      <nav className="flex gap-6">
        <Link to="/products" className="hover:text-yellow-300 transition">Products</Link>
        <Link to="/quotations" className="hover:text-yellow-300 transition">Quotations</Link>
      </nav>
    </div>
  </header>
);

export default Header;
