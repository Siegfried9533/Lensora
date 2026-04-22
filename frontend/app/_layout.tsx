import FontAwesome from "@expo/vector-icons/FontAwesome";
import {
    DarkTheme,
    DefaultTheme,
    ThemeProvider,
} from "@react-navigation/native";
import { useFonts } from "expo-font";
import { Stack } from "expo-router";
import * as SplashScreen from "expo-splash-screen";
import { useEffect } from "react";
import "react-native-reanimated";

import { useColorScheme } from "@/frontend/components/useColorScheme";

export {
    // Catch any errors thrown by the Layout component.
    ErrorBoundary
} from "expo-router";

export const unstable_settings = {
  // Ensure that reloading on `/modal` keeps a back button present.
  initialRouteName: "(tabs)",
};

// Prevent the splash screen from auto-hiding before asset loading is complete.
SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const [loaded, error] = useFonts({
    SpaceMono: require("../assets/fonts/SpaceMono-Regular.ttf"),
    ...FontAwesome.font,
  });

  // Expo Router uses Error Boundaries to catch errors in the navigation tree.
  useEffect(() => {
    if (error) throw error;
  }, [error]);

  useEffect(() => {
    if (loaded) {
      SplashScreen.hideAsync();
    }
  }, [loaded]);

  if (!loaded) {
    return null;
  }

  return <RootLayoutNav />;
}

import { AuthProvider } from "@/frontend/context/AuthContext";

function RootLayoutNav() {
  const colorScheme = useColorScheme();

  return (
    <ThemeProvider value={colorScheme === "dark" ? DarkTheme : DefaultTheme}>
      <AuthProvider>
        <Stack>
          <Stack.Screen
            name="(tabs)"
            options={{ headerShown: false, animation: "fade" }}
          />
          <Stack.Screen name="modal" options={{ presentation: "modal" }} />
          <Stack.Screen
            name="(auth)"
            options={{ headerShown: false, animation: "fade" }}
          />
          <Stack.Screen
            name="equipment/[id]"
            options={{ headerShown: false, animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="cart"
            options={{ headerShown: false, animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="checkout"
            options={{ headerShown: false, animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="orders/[id]"
            options={{ headerShown: false, animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="payment-success"
            options={{ headerShown: false, animation: "fade" }}
          />
          <Stack.Screen
            name="payment-failed"
            options={{ headerShown: false, animation: "fade" }}
          />
          <Stack.Screen
            name="notifications"
            options={{ headerShown: false, animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="rentals"
            options={{ headerShown: false, animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="store"
            options={{ headerShown: false, animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="order-status"
            options={{ headerShown: false, animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="oauth-success"
            options={{ headerShown: false, animation: "fade" }}
          />
        </Stack>
      </AuthProvider>
    </ThemeProvider>
  );
}
