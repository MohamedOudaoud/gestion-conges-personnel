<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Ajuster un solde - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <h1 class="h2 mb-4">Ajuster un solde de congé</h1>

            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/admin/balances/adjust"
                  method="post" class="card card-body shadow-sm">
                <input type="hidden" name="id" value="${solde.id}">

                <p>
                    <strong>Employé :</strong>
                    <c:out value="${solde.employe.prenom}"/>
                    <c:out value="${solde.employe.nom}"/>
                </p>
                <p>
                    <strong>Type :</strong>
                    <c:out value="${solde.typeConge.libelle}"/>
                </p>
                <p><strong>Année :</strong> <c:out value="${solde.annee}"/></p>

                <div class="mb-3">
                    <label class="form-label" for="joursAcquis">Jours acquis</label>
                    <input class="form-control" id="joursAcquis" name="joursAcquis"
                           type="number" min="0"
                           value="<c:out value='${formJoursAcquis}'/>" required>
                </div>
                <div class="mb-4">
                    <label class="form-label" for="joursPris">Jours pris</label>
                    <input class="form-control" id="joursPris" name="joursPris"
                           type="number" min="0"
                           value="<c:out value='${formJoursPris}'/>" required>
                </div>

                <div class="d-flex gap-2">
                    <button class="btn btn-primary" type="submit">Enregistrer</button>
                    <a class="btn btn-outline-secondary"
                       href="${pageContext.request.contextPath}/admin/balances?year=${solde.annee}">
                        Annuler
                    </a>
                </div>
            </form>
        </div>
    </div>
</main>
</body>
</html>
