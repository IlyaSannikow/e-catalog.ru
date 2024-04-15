<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html lang="ru">
<meta charset="UTF-8">
<head>
    <title>Обновить данные пользователя</title>
    <link rel="stylesheet" type="text/css" href="${contextPath}/resources/css/style.css">
</head>
<body>
    <h2>Информация о товаре</h2>

    <b>Название: </b>${product.name}<br>
    <b>Стоимость: </b>${product.cost}<br>
    <b>Продавец: </b>${product.source}<br>

    <h2>Комментарии: </h2>

    <c:forEach items="${allComments}" var="comment">
        -------------------------------- <br>
        Автор: ${comment.userId} <br>
        Комментарий: ${comment.message} <br>
        <c:choose>
            <c:when test="${comment.userId == user.id}">
                Удалить:
                <form action="${pageContext.request.contextPath}/product-info" method="post">
                    <input type="hidden" name="productId" value="${product.id}"/>
                    <input type="hidden" name="commentId" value="${comment.id}"/>
                    <input type="hidden" name="action" value="delete"/>
                    <button type="submit">Удалить комментарий</button>
                </form>
                Изменить:
                <form action="${pageContext.request.contextPath}/product-info" method="post">
                    <input type="hidden" name="productId" value="${product.id}"/>
                    <input type="hidden" name="commentId" value="${comment.id}"/>
                    <input type="hidden" name="action" value="update"/>
                    <button type="submit">Изменить комментарий</button>
                </form>
                <br>
            </c:when>

            <c:otherwise>
                <sec:authorize access="hasRole('ROLE_ADMIN')">
                    Удалить:
                    <form action="${pageContext.request.contextPath}/product-info" method="post">
                        <input type="hidden" name="productId" value="${product.id}"/>
                        <input type="hidden" name="commentId" value="${comment.id}"/>
                        <input type="hidden" name="action" value="delete"/>
                        <button type="submit">Удалить комментарий</button>
                    </form>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')">
                    Изменить:
                    <form action="${pageContext.request.contextPath}/product-info" method="post">
                        <input type="hidden" name="productId" value="${product.id}"/>
                        <input type="hidden" name="commentId" value="${comment.id}"/>
                        <input type="hidden" name="action" value="update"/>
                        <button type="submit">Изменить комментарий</button>
                    </form>
                    <br>
                </sec:authorize>
            </c:otherwise>
        </c:choose>
    </c:forEach>

        <h2>Добавить комментарий</h2>

        <div>
            <form action="${pageContext.request.contextPath}/product-info" method="post">
              <input type="text" name="message" value="${comment.message}"/>
              <input type="hidden" name="productId" value="${product.id}"/>
              <input type="hidden" name="action" value="create"/>
              <button type="submit">Добавить комментарий</button>
            </form>
        </div>

    <a href="/">Главная</a>
</body>
</html>