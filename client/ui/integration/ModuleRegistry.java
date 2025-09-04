package client.ui.integration;

import client.ui.api.IModuleView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 模块注册中心：集中登记/查询模块页面。 */
public final class ModuleRegistry {
    private static final List<IModuleView> MODULES = new CopyOnWriteArrayList<>();

    private ModuleRegistry() {}

    public static void register(IModuleView module) {
        if (module == null) return;
        // 去重：按 key 唯一
        for (IModuleView m : MODULES) {
            if (m.getKey().equals(module.getKey())) {
                return;
            }
        }
        MODULES.add(module);
    }

    public static List<IModuleView> getAll() {
        List<IModuleView> copy = new ArrayList<>(MODULES);
        // 尝试按显示名排序，避免无序
        Collections.sort(copy, new Comparator<IModuleView>() {
            @Override
            public int compare(IModuleView o1, IModuleView o2) {
                return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
            }
        });
        return Collections.unmodifiableList(copy);
    }

    public static IModuleView findByKey(String key) {
        if (key == null) return null;
        for (IModuleView m : MODULES) {
            if (key.equals(m.getKey())) return m;
        }
        return null;
    }
}


