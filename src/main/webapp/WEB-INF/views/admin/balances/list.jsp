<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Soldes de congé - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h2">Soldes de congé</h1>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${param.error}"/>
        </div>
    </c:if>
    <c:if test="${param.status eq 'initialized'}">
        <div class="alert alert-success" role="status">
            <c:out value="${param.count}"/> solde(s) initialisé(s) pour l'année.
        </div>
    </c:if>
    <c:if test="${param.status eq 'adjusted'}">
        <div class="alert alert-success" role="status">
            Solde ajusté avec succès.
        </div>
    </c:if>

    <form class="row g-2 align-items-end mb-4"
          action="${pageContext.request.contextPath}/admin/balances"
          method="get">
        <div class="col-auto">
            <label class="form-label" for="year">Année</label>
            <input class="form-control" id="year" name="year" type="number"
                   min="2000" value="<c:out value='${year}'/>" required>
        </div>
        <div class="col-auto">
            <button class="btn btn-outline-primary" type="submit">
                Consulter
            </button>
        </div>
    </form>

    <form action="${pageContext.request.contextPath}/admin/balances/initialize"
          method="post" class="mb-4">
        <input type="hidden" name="year" value="${year}">
        <button class="btn btn-primary" type="submit">
            Initialiser les soldes de l'année
        </button>
    </form>

    <div class="table-responsive">
        <table class="table table-striped table-bordered align-middle bg-white">
            <thead>
            <tr>
                <th scope="col">Employé</th>
                <th scope="col">Type de congé</th>
                <th scope="col">Année</th>
                <th scope="col">Jours acquis</th>
                <th scope="col">Jours pris</th>
                <th scope="col">Solde disponible</th>
                <th scope="col">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${soldes}" var="solde">
                <tr>
                    <td>
                        <c:out value="${solde.employe.prenom}"/>
                        <c:out value="${solde.employe.nom}"/>
                    </td>
                    <td><c:out value="${solde.typeConge.libelle}"/></td>
                    <td><c:out value="${solde.annee}"/></td>
                    <td><c:out value="${solde.joursAcquis ne null ? solde.joursAcquis : 0}"/></td>
                    <td><c:out value="${solde.joursPris ne null ? solde.joursPris : 0}"/></td>
                    <td><c:out value="${solde.soldeDisponible}"/></td>
                    <td>
                        <a class="btn btn-sm btn-outline-primary"
                           href="${pageContext.request.contextPath}/admin/balances/adjust?id=${solde.id}">
                            Ajuster
                        </a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty soldes}">
                <tr>
                    <td colspan="7" class="text-center">
                        Aucun solde enregistré pour cette année.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <a class="btn btn-link px-0"
       href="${pageContext.request.contextPath}/dashboard/admin">
        Retour au tableau de bord
    </a>
</main>
</body>
</html>
