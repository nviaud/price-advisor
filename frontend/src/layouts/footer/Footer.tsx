import React from 'react';

const Footer: React.FC = () => (
  <footer className="bg-gray-900 text-gray-300 py-6 mt-8">
    <div className="container mx-auto flex flex-col md:flex-row items-center justify-between px-6">
      <span className="text-sm">© {new Date().getFullYear()} Price Advisor. All rights reserved.</span>
      <span className="text-sm mt-2 md:mt-0">Contact: support@priceadvisor.com</span>
    </div>
  </footer>
);

export default Footer;

