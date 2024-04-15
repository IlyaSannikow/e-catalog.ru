<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Добавить новый товар</title>
</head>

<body>
<div>
  <form:form method="POST" modelAttribute="productForm">
    <h2>Добавить товар</h2>
    <div>
      <form:input type="text" path="name" placeholder="Введите название товара" autofocus="true"></form:input>
    </div>
    <div>
      <form:select path="category">
          <form:option value = "-" label = "Выберите категорию товара" />
          <form:options items="${categoryOptions}"/>
      </form:select>
    </div>
    <div>
      <form:input type="text" path="source" placeholder="Введите название продовца"></form:input>
    </div>
    <div>
      <form:input type="text" path="cost" placeholder="Введите стоимость товара"></form:input>
    </div>
    <button type="submit">Добавить товар</button>
  </form:form>
  <a href="/">Главная</a>
</div>
</body>
</html>