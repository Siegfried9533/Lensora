import { useAuth } from "@/frontend/context/AuthContext";
import { Notification, notificationApi } from "@/services/api/notificationApi";
import { usePathname, useRouter } from "expo-router";
import {
    Bell,
    Calendar,
    CheckCheck,
    CreditCard,
    Package,
    RefreshCw,
    Trash2,
    TrendingUp,
    Truck
} from "lucide-react-native";
import { useCallback, useEffect, useRef, useState } from "react";
import {
    ActivityIndicator,
    Alert,
    FlatList,
    Text,
    TouchableOpacity,
    View
} from "react-native";

const LOAD_TIMEOUT_MS = 10000;

export default function NotificationsScreen() {
  const router = useRouter();
  const pathname = usePathname();
  const { token, isLoading: authLoading } = useAuth();
  const isInsideTab =
    pathname === "/(tabs)/notifications" || pathname.includes("(tabs)");
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [systemNotifications, setSystemNotifications] = useState<
    Notification[]
  >([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [refreshing, setRefreshing] = useState(false);

  const lastLoadedTokenRef = useRef<string | null | undefined>(undefined);

  const doLoad = useCallback(
    async (
      authToken: string | null,
      pageNum: number = 0,
      isRefresh: boolean = false,
    ) => {
      try {
        if (isRefresh) {
          setRefreshing(true);
        } else if (pageNum === 0) {
          setIsLoading(true);
        }
        setError(null);

        // System notifications
        let allSystem: Notification[] = [];
        try {
          allSystem = await notificationApi.getSystemNotifications();
        } catch {}
        setSystemNotifications(Array.isArray(allSystem) ? allSystem : []);

        // User notifications
        if (authToken) {
          const [notificationsData, countData] = await Promise.all([
            notificationApi.getNotifications(authToken, pageNum, 20),
            notificationApi.getUnreadCount(authToken),
          ]);
          const content = Array.isArray(notificationsData?.content)
            ? notificationsData.content
            : [];
          if (isRefresh || pageNum === 0) {
            setNotifications(content);
          } else {
            setNotifications((prev) => [
              ...(Array.isArray(prev) ? prev : []),
              ...content,
            ]);
          }
          setUnreadCount(typeof countData === "number" ? countData : 0);
          setTotalPages(
            typeof notificationsData?.totalPages === "number"
              ? notificationsData.totalPages
              : 0,
          );
          setPage(
            typeof notificationsData?.page === "number"
              ? notificationsData.page
              : 0,
          );
        } else {
          setNotifications([]);
          setUnreadCount(0);
        }
      } catch (err: any) {
        console.error("[Notifications] Load error:", err?.message);
        setError(err?.message || "Failed to load notifications");
      } finally {
        setIsLoading(false);
        setRefreshing(false);
      }
    },
    [token],
  );

  useEffect(() => {
    if (lastLoadedTokenRef.current === token) return;

    if (authLoading && token === null) {
      lastLoadedTokenRef.current = null;
      doLoad(null);
      return;
    }

    lastLoadedTokenRef.current = token;
    doLoad(token);
  }, [token, authLoading, doLoad]);

  useEffect(() => {
    if (!isLoading) return;
    const timer = setTimeout(() => {
      setIsLoading(false);
      setRefreshing(false);
      setError((prev) => prev || "Loading timed out");
    }, LOAD_TIMEOUT_MS);
    return () => clearTimeout(timer);
  }, [isLoading]);

  const handleMarkAsRead = async (notificationId: string) => {
    if (!token) return;
    try {
      await notificationApi.markAsRead(token, notificationId);
      setNotifications((prev) =>
        prev.map((n) =>
          n.notificationId === notificationId
            ? { ...n, isRead: true, readAt: new Date().toISOString() }
            : n,
        ),
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch {}
  };

  const handleMarkAllAsRead = async () => {
    if (!token) return;
    try {
      const count = await notificationApi.markAllAsRead(token);
      setNotifications((prev) =>
        prev.map((n) => ({
          ...n,
          isRead: true,
          readAt: new Date().toISOString(),
        })),
      );
      setUnreadCount(0);
      Alert.alert("Success", `Marked ${count} notifications as read`);
    } catch {}
  };

  const handleDelete = async (notificationId: string) => {
    if (!token) return;
    Alert.alert("Delete Notification", "Are you sure?", [
      { text: "Cancel", style: "cancel" },
      {
        text: "Delete",
        style: "destructive",
        onPress: async () => {
          try {
            await notificationApi.deleteNotification(token, notificationId);
            setNotifications((prev) =>
              prev.filter((n) => n.notificationId !== notificationId),
            );
          } catch {
            Alert.alert("Error", "Failed to delete");
          }
        },
      },
    ]);
  };

  const handleNavigateToNotification = (notification: Notification) => {
    if (!token) {
      router.push("/(auth)/login" as any);
      return;
    }
    if (!notification.isRead) handleMarkAsRead(notification.notificationId);
    if (notification.deepLinkUrl) {
      router.push(notification.deepLinkUrl.replace("mobile://", "/") as any);
    } else if (
      notification.referenceType === "ORDER" &&
      notification.referenceId
    ) {
      router.push(`/orders/${notification.referenceId}` as any);
    } else if (
      notification.referenceType === "RENTAL" &&
      notification.referenceId
    ) {
      router.push(`/rentals/detail/${notification.referenceId}` as any);
    }
  };

  const getNotificationIcon = (type: Notification["type"]) => {
    switch (type) {
      case "ORDER_UPDATE":
        return { icon: Package, bg: "bg-blue-500/20", color: "#3b82f6" };
      case "SHIPPING_UPDATE":
        return { icon: Truck, bg: "bg-blue-500/20", color: "#3b82f6" };
      case "RENTAL_REMINDER":
      case "RENTAL_OVERDUE":
        return { icon: Calendar, bg: "bg-orange-500/20", color: "#f97316" };
      case "PAYMENT_SUCCESS":
        return { icon: CreditCard, bg: "bg-green-500/20", color: "#22c55e" };
      case "PAYMENT_FAILED":
        return { icon: CreditCard, bg: "bg-red-500/20", color: "#ef4444" };
      case "PROMOTION":
        return { icon: TrendingUp, bg: "bg-purple-500/20", color: "#a855f7" };
      case "SYSTEM":
        return { icon: Bell, bg: "bg-gray-500/20", color: "#6b7280" };
      default:
        return { icon: Bell, bg: "bg-gray-500/20", color: "#6b7280" };
    }
  };

  const getTimeAgo = (dateStr: string) => {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMins < 1) return "Just now";
    if (diffMins < 60) return `${diffMins} minutes ago`;
    if (diffHours < 24) return `${diffHours} hours ago`;
    if (diffDays === 1) return "Yesterday";
    if (diffDays < 7) return `${diffDays} days ago`;
    return date.toLocaleDateString("vi-VN");
  };

  const renderItem = ({
    item,
  }: {
    item: Notification & { _source?: string };
  }) => {
    if (!item?.notificationId) return null;
    const IconComponent = getNotificationIcon(item.type).icon;
    const iconBg = getNotificationIcon(item.type).bg;
    const iconColor = getNotificationIcon(item.type).color;

    return (
      <TouchableOpacity
        onPress={() => handleNavigateToNotification(item)}
        className={`mx-4 mb-3 p-4 rounded-2xl border ${
          !item.isRead
            ? "bg-[#0a0a0a] border-[#FF8C42]/30"
            : "bg-[#0a0a0a] border-gray-800"
        }`}
      >
        <View className="flex-row items-start">
          {/* Icon */}
          <View
            className={`w-12 h-12 rounded-xl ${iconBg} items-center justify-center flex-shrink-0`}
          >
            <IconComponent size={24} color={iconColor} />
          </View>

          {/* Content */}
          <View className="flex-1 ml-3">
            <View className="flex-row items-start justify-between mb-1">
              <View className="flex-1">
                <Text
                  className={`text-base ${!item.isRead ? "text-white font-semibold" : "text-gray-400"}`}
                  numberOfLines={1}
                >
                  {item.title || "Notification"}
                </Text>
                <Text
                  className={`text-sm mt-1 ${!item.isRead ? "text-gray-300" : "text-gray-500"}`}
                  numberOfLines={2}
                >
                  {item.message || ""}
                </Text>
              </View>
              {!item.isRead && (
                <View className="w-2 h-2 bg-[#FF8C42] rounded-full ml-2 mt-1" />
              )}
            </View>

            <View className="flex-row items-center justify-between mt-2">
              <Text className="text-xs text-gray-500">
                {getTimeAgo(item.createdAt)}
              </Text>
              {!item.isRead && (
                <TouchableOpacity
                  onPress={() => handleMarkAsRead(item.notificationId)}
                  className="px-3 py-1"
                >
                  <Text className="text-[#FF8C42] text-xs font-semibold">
                    Mark as read
                  </Text>
                </TouchableOpacity>
              )}
            </View>
          </View>

          {token && (
            <TouchableOpacity
              onPress={() => handleDelete(item.notificationId)}
              className="p-2 ml-2"
            >
              <Trash2 color="#4b5563" size={16} />
            </TouchableOpacity>
          )}
        </View>
      </TouchableOpacity>
    );
  };

  const allNotifications = [
    ...(Array.isArray(systemNotifications)
      ? systemNotifications.map((n) => ({ ...n, _source: "system" }))
      : []),
    ...(Array.isArray(notifications)
      ? notifications.map((n) => ({ ...n, _source: "user" }))
      : []),
  ];
  const hasNotifications = allNotifications.length > 0;

  if (isLoading) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <ActivityIndicator size="large" color="#FF8C42" />
        <Text className="text-gray-400 mt-4">Loading notifications...</Text>
      </View>
    );
  }

  if (error && !hasNotifications) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center px-6">
        <Bell color="#4b5563" size={64} />
        <Text className="text-gray-400 text-lg font-semibold mt-4">
          Could not load notifications
        </Text>
        <Text className="text-gray-500 text-sm mt-2 text-center">{error}</Text>
        <TouchableOpacity
          onPress={() => {
            lastLoadedTokenRef.current = undefined;
            doLoad(token);
          }}
          className="mt-4 bg-[#FF8C42] px-6 py-3 rounded-xl flex-row items-center"
        >
          <RefreshCw color="black" size={16} />
          <Text className="text-black font-bold ml-2">Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      {/* Header */}
      <View className="px-6 pt-16 pb-4 bg-[#1a1a1a]">
        <View className="flex-row items-center justify-between mb-2">
          <Text className="text-3xl text-white font-bold tracking-tight">
            NOTIFICATIONS
          </Text>
          {unreadCount > 0 && (
            <View className="bg-[#FF8C42] px-3 py-1 rounded-full">
              <Text className="text-black text-xs font-bold">
                {unreadCount} new
              </Text>
            </View>
          )}
        </View>
        <Text className="text-sm text-gray-400">
          Stay updated with your orders and rentals
        </Text>
      </View>

      {error && hasNotifications && (
        <View className="mx-4 mb-3 bg-red-900/30 px-4 py-2 flex-row items-center justify-between rounded-xl">
          <Text className="text-red-300 text-xs flex-1" numberOfLines={1}>
            {error}
          </Text>
          <TouchableOpacity
            onPress={() => {
              lastLoadedTokenRef.current = undefined;
              doLoad(token);
            }}
          >
            <RefreshCw color="#fca5a5" size={14} />
          </TouchableOpacity>
        </View>
      )}

      {!hasNotifications ? (
        <View className="flex-1 items-center justify-center px-6">
          <Bell color="#4b5563" size={64} />
          <Text className="text-gray-400 text-lg font-semibold mt-4">
            No Notifications
          </Text>
          <Text className="text-gray-500 text-sm mt-2 text-center">
            {token
              ? "When you receive notifications, they'll appear here"
              : "Sign in to see your personal notifications"}
          </Text>
          {!token && (
            <TouchableOpacity
              onPress={() => router.push("/(auth)/login" as any)}
              className="mt-4 bg-[#FF8C42] px-6 py-3 rounded-xl"
            >
              <Text className="text-black font-bold">Sign In</Text>
            </TouchableOpacity>
          )}
        </View>
      ) : (
        <FlatList
          data={allNotifications}
          renderItem={renderItem}
          keyExtractor={(item) =>
            `${item._source}-${item?.notificationId ?? Math.random().toString()}`
          }
          onRefresh={() => {
            lastLoadedTokenRef.current = undefined;
            doLoad(token, 0, true);
          }}
          refreshing={refreshing}
          onEndReached={() => {
            if (token && page < totalPages - 1) doLoad(token, page + 1);
          }}
          onEndReachedThreshold={0.5}
          showsVerticalScrollIndicator={false}
          contentContainerStyle={{ paddingBottom: 20 }}
        />
      )}

      {/* Mark All as Read Button */}
      {hasNotifications && unreadCount > 0 && token && (
        <View className="px-6 pb-6 pt-4 border-t border-gray-800 bg-[#1a1a1a]">
          <TouchableOpacity
            onPress={handleMarkAllAsRead}
            className="w-full py-4 bg-[#0a0a0a] border border-gray-800 rounded-2xl items-center flex-row justify-center"
          >
            <CheckCheck color="#FF8C42" size={20} />
            <Text className="text-white font-semibold ml-2">
              Mark all as read
            </Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}
