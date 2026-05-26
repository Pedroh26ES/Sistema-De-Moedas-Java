import { useEffect, useState } from "react";
import { api } from "../services/api";
import { navigateTo } from "../utils/navigation";

export function useDashboard(path, notify) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      setData(await api(path));
    } catch (error) {
      notify("error", error.message);
      navigateTo("/login");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [path]);

  return { data, loading, reload: load, setData };
}
