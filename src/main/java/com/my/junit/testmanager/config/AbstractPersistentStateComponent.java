package com.my.junit.testmanager.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

public abstract class AbstractPersistentStateComponent<T>
        implements PersistentStateComponent<T> {

    @Override
    public void loadState(@NotNull T t) {
        XmlSerializerUtil.copyBean(t, this);
    }

    /**
     * Сравнивает текущее состояние с другим состоянием.
     *
     * @param other другое состояние
     * @return true, если состояния равны, иначе false
     */
    public abstract boolean isStateEquals(@NotNull T other);

    /**
     * Сравнивает два списка на равенство без учета порядка элементов.
     *
     * @param list1 Список 1
     * @param list2 Список 2
     * @param <I>   Тип элементов списков
     * @return true, если списки равны, иначе false
     */
    public <I> boolean isListEquals(@Nullable List<I> list1, @Nullable List<I> list2) {
        if (list1 == null || list2 == null) {
            return list1 == list2;
        }
        if (list1.size() != list2.size()) {
            return false;
        }
        final var set1 = new HashSet<>(list1);
        final var set2 = new HashSet<>(list2);
        return set1.equals(set2);
    }

}
