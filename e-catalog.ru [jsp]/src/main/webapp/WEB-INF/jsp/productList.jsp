<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html lang="ru" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
  <meta charset="utf-8">
  <title>Товары</title>
  <link rel="stylesheet" type="text/css" href="${contextPath}/resources/css/style.css">
</head>

<body>
<div>
  <table>
    <thead>

    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <th>ID</th>
    </sec:authorize>

    <th>Название</th>
    <th>Категория</th>
    <th>Стоимость</th>
    <th>Магазин</th>

    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <th>Удалить</th>
    </sec:authorize>

    <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')">
        <th>Редактировать</th>
    </sec:authorize>

    <th>Страница магазина</th>

    </thead>

    <c:forEach items="${allProducts}" var="product">
      <tr>

        <sec:authorize access="hasRole('ROLE_ADMIN')">
            <td>${product.id}</td>
        </sec:authorize>

        <td>${product.name}</td>
        <td>${product.category}</td>
        <td>${product.cost}</td>
        <td>${product.source}</td>

        <sec:authorize access="hasRole('ROLE_ADMIN')">
            <td>
              <form action="${pageContext.request.contextPath}/productList" method="post">
                <input type="hidden" name="productId" value="${product.id}"/>
                <input type="hidden" name="action" value="delete"/>
                <button type="submit">Удалить товар</button>
              </form>
            </td>
        </sec:authorize>

        <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')">
            <td>
              <form action="${pageContext.request.contextPath}/productList" method="post">
                <input type="hidden" name="productId" value="${product.id}"/>
                <input type="hidden" name="action" value="update"/>
                <button type="submit">Обновить данные о товаре</button>
              </form>
            </td>
        </sec:authorize>

        <td><a href = "/productList/${product.id}">Страница товара</a></td>

      </tr>
    </c:forEach>
  </table>
  <a href="/">Главная</a>
</div>
</body>
</html>