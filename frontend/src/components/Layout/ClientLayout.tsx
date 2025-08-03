'use client';

import {QueryClient, QueryClientProvider} from "react-query";

export const ClientLayout = ({children}: {
  children: React.ReactNode;
}) => {

// Create a client
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        refetchOnWindowFocus: false,
        retry: 1,
        staleTime: 5 * 60 * 1000, // 5 minutes
      },
    }})


    return (
      <QueryClientProvider client={queryClient}>
        {children}
      </QueryClientProvider>
    )

}