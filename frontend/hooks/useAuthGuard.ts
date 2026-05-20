/**
 * useAuthGuard Hook
 *
 * Reusable hook for protecting screens that require authentication.
 * Redirects to login screen if user is not authenticated.
 *
 * Usage:
 * ```tsx
 * export default function ProtectedScreen() {
 *   const { requiresAuth } = useAuthGuard();
 *
 *   useEffect(() => {
 *     requiresAuth();
 *   }, []);
 *
 *   if (!isAuthenticated) {
 *     return <LoadingScreen />;
 *   }
 *
 *   return <YourScreen />;
 * }
 * ```
 */

<<<<<<< HEAD
import { useAuth } from '@/context/AuthContext';
=======
import { useAuth } from "@/frontend/context/AuthContext";
>>>>>>> LTH
import { useRouter } from "expo-router";
import { useEffect, useState } from "react";
import { Alert } from "react-native";

interface UseAuthGuardResult {
  isAuthenticated: boolean;
  isLoading: boolean;
  requiresAuth: (options?: AuthGuardOptions) => void;
}

interface AuthGuardOptions {
  /** Show alert before redirecting */
  showAlert?: boolean;
  /** Alert title */
  alertTitle?: string;
  /** Alert message */
  alertMessage?: string;
  /** Route to redirect to (default: login) */
  redirectRoute?: string;
}

export function useAuthGuard(): UseAuthGuardResult {
  const router = useRouter();
  const { token, user, isLoading: authLoading } = useAuth();
  const [isLoading, setIsLoading] = useState(true);

  const isAuthenticated = !!token && !!user;

  useEffect(() => {
    // Stop loading once auth state is determined
    if (!authLoading) {
      setIsLoading(false);
    }
  }, [authLoading]);

  const requiresAuth = (options: AuthGuardOptions = {}) => {
    const {
      showAlert = true,
      alertTitle = "Login Required",
      alertMessage = "Please login to access this screen",
      redirectRoute = "/(auth)/login",
    } = options;

    if (!authLoading && !isAuthenticated) {
      if (showAlert) {
        Alert.alert(alertTitle, alertMessage, [
          {
            text: "Cancel",
            onPress: () => router.back(),
            style: "cancel",
          },
          {
            text: "Login",
            onPress: () => {
              router.replace(redirectRoute as any);
            },
          },
        ]);
      } else {
        router.replace(redirectRoute as any);
      }
    }
  };

  return {
    isAuthenticated,
    isLoading: isLoading || authLoading,
    requiresAuth,
  };
}

export default useAuthGuard;
