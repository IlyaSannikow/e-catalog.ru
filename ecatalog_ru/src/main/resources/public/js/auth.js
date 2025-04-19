async function logout() {
    const shouldLogout = confirm("Вы уверены, что хотите выйти из системы?");
    if (!shouldLogout) return;

    try {
        const csrfToken = document.cookie
            .split('; ')
            .find(row => row.startsWith('XSRF-TOKEN='))
            ?.split('=')[1];

        const response = await fetch("/logout", {
            method: "POST",
            headers: {
                "X-XSRF-TOKEN": csrfToken,
                "X-Requested-With": "XMLHttpRequest"
            },
            credentials: "include"
        });

        if (response.ok) {
            window.location.href = "/";
        } else {
            throw new Error('Ошибка при выходе');
        }
    } catch (error) {
        console.error('Ошибка при выходе:', error);
        alert("Не удалось выйти из системы");
    }
}