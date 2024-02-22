<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Добавить группу</title>
</head>

<body>
<div>
  <form:form method="POST" modelAttribute="postForm">
    <h2>Добавить пост</h2>
    <div>
      <form:input type="text" path="name" placeholder="Введите ФИО" autofocus="true"></form:input>
    </div>

    <div>
      <form:input type="text" path="message" placeholder="Введите проишествие и данные" autofocus="true"></form:input>
    </div>

    <div>
      <form:input type="date" path="incidentDay" autofocus="true"></form:input> Дата происшествия
    </div>

    <div>
      <form:input type="text" path="outfit" placeholder="Введите одежду" autofocus="true"></form:input>
    </div>

    <div>
      <form:input type="text" path="phoneNumber" placeholder="Введите номер телефона" autofocus="true"></form:input>
    </div>

    <button type="submit">Добавить</button>
  </form:form>
  <a href="/">Главная</a>
</div>
</body>
</html>