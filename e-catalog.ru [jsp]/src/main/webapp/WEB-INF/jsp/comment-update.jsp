<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html lang="ru">
<meta charset="UTF-8">
<title>Обновить данные о товаре</title>
</head>
<body>

    <h2>Обновить данные товара ${id}</h2>
    <form action="${pageContext.request.contextPath}/product-info" method="post">
        <input type="text" name="message" value="${comment.message}" placeHolder="Обновить сообщение"/> <br>
        <input type="hidden" name="commentId" value="${comment.id}"/> <br>
        <input type="hidden" name="productId" value="${product.id}"/> <br>
        <input type="hidden" name="action" value="update2"/>
        <button type="submit">Обновить комментарий</button>
    </form>
</body>
</html>