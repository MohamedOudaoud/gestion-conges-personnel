<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Demandes à valider - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h2">Demandes de congé en attente</h1>
    </div>

    <c:if test="${param.status eq 'updated'}">
        <div class="alert alert-success" role="status">
            La décision a été enregistrée.
        </div>
    </c:if>

    <div class="table-responsive">
        <table class="table table-striped table-bordered align-middle bg-white">
            <thead>
            <tr>
                <th scope="col">Employé</th>
                <th scope="col">Type</th>
                <th scope="col">Début</th>
                <th scope="col">Fin</th>
                <th scope="col">Jours ouvrés</th>
                <th scope="col">Motif</th>
                <th scope="col">Décision</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${demandes}" var="demande">
                <tr>
                    <td>
                        <c:out value="${demande.employe.prenom}"/>
                        <c:out value="${demande.employe.nom}"/>
                    </td>
                    <td><c:out value="${demande.typeConge.libelle}"/></td>
                    <td><c:out value="${demande.dateDebut}"/></td>
                    <td><c:out value="${demande.dateFin}"/></td>
                    <td><c:out value="${demande.nbJours ne null ? demande.nbJours : 0}"/></td>
                    <td><c:out value="${demande.motif}"/></td>
                    <td>
                        <form class="d-inline"
                              action="${pageContext.request.contextPath}/${validatorRole eq 'ADMIN' ? 'admin' : 'manager'}/leave-requests/approve"
                              method="post">
                            <input type="hidden" name="id" value="${demande.id}">
                            <button class="btn btn-sm btn-success mb-1" type="submit">
                                Accepter
                            </button>
                        </form>

                        <form class="mt-2"
                              action="${pageContext.request.contextPath}/${validatorRole eq 'ADMIN' ? 'admin' : 'manager'}/leave-requests/reject"
                              method="post">
                            <input type="hidden" name="id" value="${demande.id}">
                            <label class="visually-hidden"
                                   for="commentaireDecision${demande.id}">
                                Motif du refus
                            </label>
                            <textarea class="form-control form-control-sm mb-1"
                                      id="commentaireDecision${demande.id}"
                                      name="commentaireDecision"
                                      maxlength="1000"
                                      rows="2"
                                      required
                                      placeholder="Motif du refus"></textarea>
                            <button class="btn btn-sm btn-outline-danger" type="submit">
                                Refuser
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty demandes}">
                <tr>
                    <td colspan="7" class="text-center">
                        Aucune demande en attente.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <a class="btn btn-link px-0"
       href="${pageContext.request.contextPath}/dashboard">
        Retour au tableau de bord
    </a>
</main>
</body>
</html>
