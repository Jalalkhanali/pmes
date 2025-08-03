import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import { Toaster } from 'react-hot-toast';
import Sidebar from '@/components/Layout/Sidebar';
import {ClientLayout} from "@/components/Layout/ClientLayout";

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'PMES - Energy Planning System',
  description: 'Professional energy planning software with AI-powered forecasting and emissions analysis',
  keywords: 'energy planning, forecasting, emissions, AI, neural networks',
  authors: [{ name: 'PMES Team' }],
  viewport: 'width=device-width, initial-scale=1',
};


export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
      <ClientLayout>
          <div className="flex h-screen bg-gray-100">
            <Sidebar />
            <div className="flex-1 flex flex-col overflow-hidden">
              <main className="flex-1 overflow-y-auto bg-gray-50">
                {children}
              </main>
            </div>
          </div>
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 4000,
              style: {
                background: '#363636',
                color: '#fff',
              },
              success: {
                duration: 3000,
                iconTheme: {
                  primary: '#22c55e',
                  secondary: '#fff',
                },
              },
              error: {
                duration: 5000,
                iconTheme: {
                  primary: '#ef4444',
                  secondary: '#fff',
                },
              },
            }}
          />
      </ClientLayout>
      </body>
    </html>
  );
} 