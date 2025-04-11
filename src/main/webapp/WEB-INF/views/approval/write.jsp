    <!DOCTYPE html>
    <html lang="en">
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <script src="https://code.jquery.com/jquery-latest.min.js"></script>
        <link rel="stylesheet" href="/css/approval/draft.css">
        <script src="/js/summernote/summernote-lite.js"></script>
        <script src="/js/summernote/lang/summernote-ko-KR.js"></script>
        <link rel="stylesheet" href="/js/summernote/summernote-lite.css">
        <title>Document</title>
        <style>
            .loading-img {
                //background-image: '/image/';
            }
        </style>
    </head>
    <div class="loading-img" style="display: none"></div>
    <body>

    <div class="overlay"></div>
    <div class="modalContainer">
        <div class="approvalModalSelect">
            <div>
                <h3>사원 목록</h3>
                <div id="employees">
                    <div id="departments1">
                        <label for="selector">부서 목록</label>
                        <select id="selector" onchange="employees(this.value)">
                            <option>부서</option>
                            <option value="1">경영</option>
                            <option value="2">인사</option>
                            <option value="3">총무</option>
                            <option value="4">운영지원</option>
                            <option value="5">연구</option>
                            <option value="6">사장</option>
                        </select>
                    </div>
                </div>
            </div>

            <div>
                <h3>결재선</h3>
                <div id="approvalLine" ondrop="drop(event)" ondragover="allowDrop(event)">

                </div>
            </div>
        </div>
        <div>
            <button id="addApproval">추가</button>
            <button id="closeModal">닫기</button>
        </div>
    </div>

    <form action="/approval/insert" id="form">
     <jsp:include page="${dto.form}" />
    </form>

    <script src="/js/template/summernote.js" type="text/javascript"></script>
    <script>
        window.onload = function() {
            const date = new Date();
            $('.date').html(date.toLocaleDateString());
        }
        const approvalList = [];

        $('#addApproval').on('click', function () {
            const lineBox = $('#lineBox');
            const approvalLine = $('#approvalLine');

            approvalList.forEach(employee => {

                if ($('#lineBox').find(`#${employee.name}`).length === 0) {
                    const div = makeEmployee(employee);
                    div.append($('<input>').attr('type', 'hidden').attr('name', 'approverId').val(employee.id))

                    lineBox.append(div);
                }
            });

            $('.modalContainer').fadeOut();
            $('.overlay').fadeOut();
        });


        function allowDrop(event) {
            event.preventDefault();
        }

        function drag(event) {
            event.dataTransfer.setData("text", event.target.textContent);

        }


        function drop(event) {
            event.preventDefault();
            let employee = JSON.parse(event.dataTransfer.getData("application/json"));
            const exists = approvalList.some(emp => emp.id === employee.id);
            if (exists) {
                alert("이미 추가된 사원입니다.");
                return;
            }
            if (employee) {
                $('#' + employee.id).remove();
                const div = makeEmployee(employee);
                $('#approvalLine').append(div);
                approvalList.push(employee);
            }

        }

        $('#add').on('click', function () {
            $('.modalContainer').fadeIn();
            $('.overlay').fadeIn();
        });

        $('#closeModal').on('click', function () {
            $('.modalContainer').fadeOut();
            $('.overlay').fadeOut();
        });

        $('#contents').summernote({
            height: 300,
            lang: 'ko-KR'
        });

        function employees(departmentId) {
            $.ajax({
                url: '/employee/department/' + departmentId,
                type: 'GET',
                dataType: 'json',
                success: function (response) {
                    let employeesHtml = '';
                    $('#employees').find('div:not(#departments1)').remove();
                    if (response.length === 0) {
                        employeesHtml = "<p>해당 부서에 사원이 없습니다.</p>";
                    } else {
                        response.forEach(function (employee) {
                            const div = makeEmployee(employee)

                            $('#employees').append(div);
                        });
                    }
                },
                error : function(request, status, error) {
                    console.log("code: " + request.status)
                    console.log("message: " + request.responseText)
                    console.log("error: " + error);
                }
            });
        }
        function makeEmployee(employee) {
            const div = $('<div>')
                .addClass("employee")
                .attr('draggable', true)
                .attr('id', employee.id);

            div.append($("<span>").addClass("jobName").html(employee.jobName));
            div.append($("<span>").html(" : "));
            div.append($("<span>").addClass("name").html(employee.name));

            // 삭제 버튼 추가
            const removeBtn = $('<button>')
                .addClass("removeBtn")
                .text("❌")
                .css({
                    marginLeft: '10px',
                    color: 'red',
                    cursor: 'pointer',
                    background: 'none',
                    border: 'none',
                    fontSize: '14px'
                })
                .on('click', function () {
                    div.remove(); // DOM에서 제거
                    const index = approvalList.findIndex(emp => emp.id === employee.id);
                    if (index > -1) approvalList.splice(index, 1); // 배열에서 제거
                });

            div.append(removeBtn);

            div.on('dragstart', function (event) {
                event.originalEvent.dataTransfer.setData("application/json", JSON.stringify(employee));
            });

            return div;
        }

        $('form').on('submit', function(e) {
            e.preventDefault();

            if (approvalList.length < 1) {
                alert('결재자는 최소 2명 이상이어야 합니다.');
                return;
            }

            const formData = new FormData(document.getElementById('form'));
            formData.append('content', $('#contents').summernote('code'));

            approvalList.forEach(employee => {
                formData.append('approverId', employee.id);
            });
            $.ajax({
                url: '/approval/insert',
                method: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                error : function(request, status, error) {
                    console.log("code: " + request.status)
                    console.log("message: " + request.responseText)
                    console.log("error: " + error);

                }
            }).done(function(resp) {
                console.log('전송됨');
                if (window.opener) {
                    window.opener.location.reload();
                    window.close();
                } else {
                    location.href = '/approval/list';
                }
            });
        });
    </script>

    </body>

    </html>
