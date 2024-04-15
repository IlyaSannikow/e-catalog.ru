<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html lang="ru">
<meta charset="UTF-8">
<title>Обновить данные о товаре</title>
</head>
<body>
    <form action="${pageContext.request.contextPath}/productList" method="post">
        <input type="text" name="name" value="${product.name}" placeHolder="Обновить название товара"/> <br>
        <input type="text" name="category" value="${product.category}" placeHolder="Обновить категорию товара"/> <br>
        <input type="text" name="cost" value="${product.cost}" placeHolder="Обновить стоимость товара"/> <br>
        <input type="text" name="source" value="${product.source}" placeHolder="Обновить продовца товара"/> <br>
        <input type="hidden" name="productId" value="${product.id}"/> <br>
        <input type="hidden" name="action" value="update2"/>
        <button type="submit">Обновить</button>
    </form>
</body>
</html>