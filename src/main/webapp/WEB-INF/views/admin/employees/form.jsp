<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title><c:out value="${formTitle}"/> - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <h1 class="h2 mb-4"><c:out value="${formTitle}"/></h1>

            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <c:set var="selectedRole"
                   value="${not empty param.role ? param.role : currentRole}"/>
            <c:set var="selectedDepartmentId"
                   value="${not empty param.departementId ? param.departementId : currentDepartmentId}"/>

            <form action="${formAction}" method="post" class="card card-body shadow-sm">
                <c:if test="${isEdit}">
                    <input type="hidden" name="id" value="${employe.id}">
                </c:if>

                <div class="mb-3">
                    <label class="form-label" for="matricule">Matricule</label>
                    <input class="form-control" id="matricule" name="matricule"
                           value="<c:out value='${not empty param.matricule ? param.matricule : employe.matricule}'/>"
                           required>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label" for="prenom">Prénom</label>
                        <input class="form-control" id="prenom" name="prenom"
                               value="<c:out value='${not empty param.prenom ? param.prenom : employe.prenom}'/>"
                               required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label" for="nom">Nom</label>
                        <input class="form-control" id="nom" name="nom"
                               value="<c:out value='${not empty param.nom ? param.nom : employe.nom}'/>"
                               required>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="email">Adresse email</label>
                    <input class="form-control" type="email" id="email" name="email"
                           value="<c:out value='${not empty param.email ? param.email : employe.email}'/>"
                           required>
                </div>

                <c:if test="${not isEdit}">
                    <div class="mb-3">
                        <label class="form-label" for="initialPassword">
                            Mot de passe initial
                        </label>
                        <input class="form-control" type="password"
                               id="initialPassword" name="initialPassword"
                               autocomplete="new-password" required>
                    </div>
                </c:if>

                <div class="mb-3">
                    <label class="form-label" for="role">Rôle</label>
                    <select class="form-select" id="role" name="role" required>
                        <option value="">-- Choisir un rôle --</option>
                        <c:forEach items="${roles}" var="role">
                            <option value="${role}"
                                    <c:if test="${role eq selectedRole}">selected</c:if>>
                                <c:out value="${role}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="departementId">Département</label>
                    <select class="form-select" id="departementId"
                            name="departementId" required>
                        <option value="">-- Choisir un département --</option>
                        <c:forEach items="${departements}" var="departement">
                            <option value="${departement.id}"
                                    <c:if test="${departement.id.toString() eq selectedDepartmentId}">selected</c:if>>
                                <c:out value="${departement.nom}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="mb-4">
                    <label class="form-label" for="dateEmbauche">Date d'embauche</label>
                    <input class="form-control" type="date" id="dateEmbauche"
                           name="dateEmbauche"
                           value="<c:out value='${not empty param.dateEmbauche ? param.dateEmbauche : employe.dateEmbauche}'/>">
                </div>

                <div class="d-flex gap-2">
                    <button class="btn btn-primary" type="submit">Enregistrer</button>
                    <a class="btn btn-outline-secondary"
                       href="${pageContext.request.contextPath}/admin/employees">
                        Annuler
                    </a>
                </div>
            </form>
        </div>
    </div>
</main>
</body>
</html>
