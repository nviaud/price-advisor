import React, { ReactNode } from 'react';
import Header from './header/Header';
import Footer from './footer/Footer';

interface BaseLayoutProps {
  children: ReactNode;
}

const BaseLayout: React.FC<BaseLayoutProps> = ({ children }) => (
  <div className="flex flex-col min-h-screen bg-gray-50">
    <Header />
    <main role="main" className="flex-1 container mx-auto px-4 py-8">
      {children}
    </main>
    <Footer />
  </div>
);

export default BaseLayout;
