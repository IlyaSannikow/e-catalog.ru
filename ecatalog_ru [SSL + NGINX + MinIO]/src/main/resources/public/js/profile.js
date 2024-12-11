(() => {
        const profileImageForm = document.querySelector("form#profile-image-form");
        profileImageForm.onsubmit = e => {
            e.preventDefault();

            const formData = new FormData(profileImageForm);

            fetch("/csrf")
                .then(response => {
                    if (response.status === 200) {
                        return response.json();
                    } else {
                        throw new Error("Не удалось получить CSRF токен.");
                    }
                })
                .then(csrf => {
                    const headers = {};
                    headers[csrf.headerName] = csrf.token; // Используйте оригинальный токен
                    return fetch("/upload", {
                        method: "POST",
                        body: formData,
                        headers: headers
                    });
                })
                .then(response => {
                    if (!response.ok) {
                        console.error("Ошибка при загрузке изображения.");
                    } else {
                        console.log("Изображение успешно загружено.");
                    }
                })
                .catch(error => {
                    console.error('Произошла ошибка:', error);
                });
        }

        const changePassForm = document.querySelector("form#change-password-form");
        changePassForm.onsubmit = e => {
            e.preventDefault();
            const newPassword = changePassForm.querySelector("input[name='newPassword']").value.trim();
            if (!newPassword) {
                return;
            }

            const data = new URLSearchParams();
            data.set("password", newPassword);

            fetch("/csrf")
                .then(response => {
                    if (response.status === 200) {
                        return response.json();
                    } else {
                        throw new Error("Не удалось получить CSRF токен.");
                    }
                })
                .then(csrf => {
                    const headers = {};
                    headers[csrf.headerName] = csrf.token; // Используйте оригинальный токен
                    return fetch("/changePassword", {
                        method: "POST",
                        body: data,
                        headers: headers
                    });
                })
                .then(response => {
                    // Убрал сообщения об успехе или ошибке
                    if (!response.ok) {
                        console.error("Ошибка при изменении пароля.");
                    }
                })
                .catch(error => {
                    console.error('Произошла ошибка:', error);
                });
        }
    })();