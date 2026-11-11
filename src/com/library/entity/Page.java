package com.library.entity;

import java.util.List;

/**
 * 简易分页对象。
 */
public class Page<T> {
    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final int total;

    public Page(List<T> items, int page, int pageSize, int total) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        if (total <= 0) {
            return 1;
        }
        return (total + pageSize - 1) / pageSize;
    }

    public boolean getHasPrev() {
        return page > 1;
    }

    public boolean getHasNext() {
        return page < getTotalPages();
    }

    public int getPrevPage() {
        return Math.max(1, page - 1);
    }

    public int getNextPage() {
        return Math.min(getTotalPages(), page + 1);
    }

    public int getStartIndex() {
        return total == 0 ? 0 : (page - 1) * pageSize + 1;
    }

    public int getEndIndex() {
        return Math.min(page * pageSize, total);
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
