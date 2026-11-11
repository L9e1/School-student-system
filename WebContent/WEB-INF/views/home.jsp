<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="home"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>首页 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<main class="page">
    <section class="hero">
        <div class="hero-copy">
            <span class="eyebrow">WELCOME TO THE LIBRARY</span>
            <h1>${greeting}，<c:out value="${loginUser.displayName}"/>。<br>今天想从<em>哪个板块</em>开始？</h1>
            <p>书阁里共有 ${categoryCount} 个板块、${titleCount} 种在馆图书。点开一个板块，就能走进对应的书架，查找、上架或整理藏书。</p>
            <div class="hero-actions">
                <a class="btn btn-primary" href="#sections">浏览图书板块</a>
                <a class="btn btn-ghost" href="${ctx}/search">全馆检索</a>
            </div>
        </div>

        <div class="hero-stats" aria-label="馆藏统计">
            <div class="stat">
                <strong>${titleCount}</strong>
                <span>在馆书目</span>
            </div>
            <div class="stat">
                <strong>${stockCount}</strong>
                <span>馆藏册数</span>
            </div>
            <div class="stat">
                <strong>${categoryCount}</strong>
                <span>图书板块</span>
            </div>
            <div class="stat">
                <strong>${userCount}</strong>
                <span>注册读者</span>
            </div>
        </div>
    </section>

    <section class="section" id="sections">
        <div class="section-head">
            <div>
                <span class="kicker">COLLECTIONS</span>
                <h2>图书板块</h2>
                <p>按主题分类的藏书空间，点击卡片进入对应书架</p>
            </div>
            <a class="btn btn-soft btn-sm" href="${ctx}/category?action=toAdd">＋ 新建板块</a>
        </div>

        <div class="category-grid">
            <c:forEach var="cat" items="${categories}" varStatus="st">
                <article class="category-card cat-${cat.color}" style="--i:${st.index}">
                    <a class="category-link" href="${ctx}/category?id=${cat.id}">
                        <span class="cat-icon">${cat.icon}</span>
                        <span class="cat-count">${cat.bookCount} 本藏书</span>
                        <h3><c:out value="${cat.name}"/></h3>
                        <p><c:out value="${cat.description}"/></p>
                        <span class="cat-arrow">进入书架 →</span>
                    </a>
                    <div class="cat-tools">
                        <a href="${ctx}/category?action=toEdit&id=${cat.id}">编辑</a>
                        <form action="${ctx}/category" method="post"
                              data-confirm="删除板块「${fn:escapeXml(cat.name)}」会同时删除其中 ${cat.bookCount} 本图书，确定继续吗？">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${cat.id}">
                            <button type="submit" class="link-danger">删除</button>
                        </form>
                    </div>
                </article>
            </c:forEach>

            <a class="add-category" href="${ctx}/category?action=toAdd">
                <span class="plus">＋</span>
                <strong>新建图书板块</strong>
                <small>为下一类藏书留个位置</small>
            </a>
        </div>
    </section>

    <section class="section">
        <div class="section-head">
            <div>
                <span class="kicker">NEW ARRIVALS</span>
                <h2>最近上架</h2>
                <p>刚刚被放进书架的新书</p>
            </div>
            <a class="btn btn-ghost btn-sm" href="${ctx}/search?sort=oldest">查看全部图书 →</a>
        </div>

        <c:choose>
            <c:when test="${empty recentBooks}">
                <div class="empty">
                    <div class="empty-icon">📚</div>
                    <h3>书架还是空的</h3>
                    <p>先创建一个板块，再把第一本书放进来吧。</p>
                    <a class="btn btn-primary btn-sm" href="${ctx}/category?action=toAdd">新建板块</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="recent-grid">
                    <c:forEach var="book" items="${recentBooks}">
                        <a class="mini-book" href="${ctx}/book?action=detail&id=${book.id}">
                            <span class="mini-cover cat-${book.categoryColor}">${book.initial}</span>
                            <span class="mini-info">
                                <strong><c:out value="${book.name}"/></strong>
                                <span><c:out value="${book.author}"/> · <c:out value="${book.categoryName}"/></span>
                            </span>
                            <span class="mini-price">￥${book.price}</span>
                        </a>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<%@ include file="common/footer.jspf" %>
</body>
</html>
