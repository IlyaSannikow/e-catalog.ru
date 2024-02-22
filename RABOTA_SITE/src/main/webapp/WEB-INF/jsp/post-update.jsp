<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html lang="ru">
<meta charset="UTF-8">
<title>Обновить данные поста</title>
</head>
<body>
    <form action="${pageContext.request.contextPath}/postList" method="post">
        <input type="text" name="name" value="${post.name}" placeHolder="Обновить ФИО"/> <br>
	    <input type="text" name="message" value="${post.message}" placeHolder="Обновить сообщение"/> <br>
	    <input type="date" name="incidentDay" value="${post.incidentDay}"/> Дата происшествия<br>
	    <input type="text" name="outfit" value="${post.outfit}" placeHolder="Обновить внешний вид"/> <br>
	    <input type="text" name="phoneNumber" value="${post.phoneNumber}" placeHolder="Обновить номер телефона"/> <br>
        <input type="hidden" name="postId" value="${post.id}"/> <br>
        <input type="hidden" name="action" value="update2"/>
        <button type="submit">Обновить</button>
    </form>
</body>
</html>