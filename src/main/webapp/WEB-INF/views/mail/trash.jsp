<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/views/template/header.jsp"/>
<link rel="stylesheet" href="/css/mail/trash.css" />
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons"/>
<style>
    * {
        /*border: 1px solid red;*/
        box-sizing: border-box;
    }
    .mainHeader {
        position: fixed;
        left: 60px;
        top: 0;
        width: 200px;
        height: 100vh;
        background-color: #fff;
        border-right: 1px solid #e0e0e0;
        z-index: 100;
        margin-top: 50px;
        transition: none;
    }

    .mainContainer {
        width: calc(100% - 200px);  /* boxContents 전체 너비에서 mainHeader(200px)만큼 뺌 */
        margin-left: 200px;  /* mainHeader 너비만큼 왼쪽 마진 */
        min-height: 100vh;
        background-color: #fff;
        padding: 20px;
        display: flex;
        justify-content: center;
        transition: none;
    }

    .mainBody {
        max-width: 1200px;
        width: 100%;
        padding: 0;
        gap: 20px;  /* 자식 요소들 간의 간격 */
        display: flex;  /* flex 컨테이너로 설정 */
        flex-direction: column;  /* 세로 방향 정렬 */
    }


    /* 메뉴 헤더 컨테이너 */
    .detail-menu-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 10px 10px 0 10px;
        width: 100%;
        border-bottom: 1px solid #e0e0e0;
    }


    .detail-menu-title {
        font-size: 18px;                    /* 폰트 크기 증가 */
        font-weight: 600;
        margin: 10px 0;                     /* 상단 여백 추가 */
        display: flex;
        align-items: center;
        justify-content: start;
        gap: 8px;
    }

    .detail-menu-title .material-icons {
        font-size: 22px;                    /* 아이콘 크기도 약간 증가 */
    }


    .detail-menu-list {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .detail-menu-item {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        gap: 10px;
        cursor: pointer;
        transition: background-color 0.2s;
        border-radius: 4px;
    }

    .detail-menu-item:hover {
        background-color: #f5f5f5;
    }

    .detail-menu-disc {
        font-size: 12px;
    }

    .detail-menu-item>.material-icons {
        font-size: 18px;
        color: #757575;
    }

    .detail-menu-item span:not(.material-icons):not(.detail-badge) {
        font-size: 12px;
        flex: 1;
    }

    .detail-badge {
        background-color: #e0e0e0;
        color: #424242;
        padding: 2px 8px;
        border-radius: 12px;
        font-size: 12px;
        min-width: 20px;
        text-align: center;
    }

    .detail-menu-item.active {
        background-color: #e3f2fd;
        color: #1976d2;
    }

    .detail-menu-item.active .material-icons {
        color: #1976d2;
    }

    .detail-menu-item.active .detail-badge {
        background-color: #1976d2;
        color: white;
    }

    .detail-menu-item a {
        display: flex;
        align-items: center;
        gap: 10px;
        text-decoration: none;
        color: inherit;
        width: 100%;
        height: 100%;
    }

    .detail-menu-item a .material-icons {
        font-size: 18px;
        color: #757575;
    }

    .detail-menu-item a:hover {
        text-decoration: none;
        color: inherit;
    }

    .search {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        gap: 5px;
    }

    .search * {
        height: 25px !important;
    }

    .search input,
    .search button,
    .search select {
        height: 25px;
        line-height: 25px;
        padding: 0 8px;
    }

    #searchBtn {
        font-size: 13px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 12px;
    }


    .content {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: white;
    }

    .pageNavi {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .detail-menu-modal {
        position: relative;
    }

    .detail-menu-toggle-btn {
        display: none;
        padding: 8px;
        background: #fff;
        border: 1px solid #e0e0e0;
        border-radius: 4px;
        cursor: pointer;
        align-items: center;
        justify-content: center;
        margin: 10px 0;
    }

    .detail-menu-toggle-btn:hover {
        background: #f5f5f5;
    }

    .detail-menu-toggle-btn .material-icons {
        font-size: 24px;
    }

    .detail-modal-close {
        display: none;
        position: absolute;
        top: 10px;
        right: 10px;
        background: transparent;
        border: none;
        cursor: pointer;
        padding: 5px;
    }

    .detail-modal-close:hover {
        opacity: 0.7;
    }

    .detail-modal-close .material-icons {
        font-size: 24px;
        color: #666;
    }

    @media (max-width: 768px) {
        .mainHeader {
            position: relative;
            left: 0;
            width: 100%;
            height: auto;
        }

        .mainContainer {
            margin-left: 0;
            width: 100%;
        }

        .detail-menu-toggle-btn {
            display: flex;
        }

        .detail-modal-close {
            display: block;
            position: absolute;
            top: 10px;
            right: 10px;
            background: transparent;
            border: none;
            cursor: pointer;
            padding: 5px;
        }

        .detail-modal-close:hover {
            opacity: 0.7;
        }

        .detail-menu-list {
            margin-top: 30px;
            max-height: 80vh;
            overflow-y: auto;
            padding-top: 10px;
        }

        .detail-menu-modal {
            display: none;
            position: absolute;
            top: 100%;
            width: 250px;
            right: 0;
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            z-index: 1100;
            margin-top: 10px;
        }

        .detail-menu-modal.active {
            display: block;
        }
    }

    @media screen and (max-width: 768px) {
        .mainHeader {
            margin-top: 10px;
        }

        .mainContainer {
            margin-top: 10px;
            width: 100%;
            margin-left: 0;
            min-height: calc(100vh - 70px);
        }
    }
</style>
<div class="mainHeader">
    <div class="detail-menu-header">
        <div class="detail-menu-title">
            <span class="material-icons">mail</span>
            <span>메일함</span>
        </div>
        <button class="detail-menu-toggle-btn">
            <span class="material-icons">menu</span>
        </button>
    </div>
    <div class="detail-menu-modal">
        <ul class="detail-menu-list">
            <a href="/mail/important">
                <li class="detail-menu-item">
                    <span class="material-icons">star</span>
                    <span>중요 메일함</span>
                    <span class="detail-badge"><span id="importantMailBox"></span></span>
                </li>
            </a>
            <a href="/mail/list">
                <li class="detail-menu-item">
                    <span class="material-icons">all_inbox</span>
                    <span>전체 메일함</span>
                    <span class="detail-badge"><span id="allMailBox"></span></span>
                </li>
            </a>
            <a href="/mail/sendList">
                <li class="detail-menu-item">
                    <span class="material-icons">send</span>
                    <span>보낸 메일함</span>
                </li>
            </a>
            <a href="/mail/inbox">
                <li class="detail-menu-item">
                    <span class="material-icons">move_to_inbox</span>
                    <span class="detail-menu-disc">받은 메일함</span>
                    <span class="detail-badge"><span id="receiveMailBox"></span></span>
                </li>
            </a>
            <a href="/mail/temp">
                <li class="detail-menu-item">
                    <span class="material-icons">drafts</span>
                    <span>임시 저장함</span>
                    <span class="detail-badge">2</span>
                </li>
            </a>
            <a href="/mail/trash">
                <li class="detail-menu-item">
                    <span class="material-icons">delete</span>
                    <span>휴지통</span>
                </li>
            </a>
        </ul>
        <button class="detail-modal-close">
            <span class="material-icons">close</span>
        </button>
    </div>
</div>
<div class="mainContainer">
    <div class="mainBody">
        <div class="search">
            <div>
                <select id="searchOption">
                    <option>보낸 사람 </option>
                    <option>내용 </option>
                    <option>제목 </option>
                </select>
            </div>
            <div class="searchInput">
                <input id="input" type="text" name="keyword" placeholder="전체 메일함">
            </div>
            <div>
                <button id="searchBtn"><span class="material-icons">search</span> 검색</button>
            </div>
        </div>
        <div class="mailWriteBtnContainer">
            <button onclick="location.href='/mail/write'">메일쓰기</button>
        </div>
        <div class="titleArea">
            <h4>전체 메일 (<span id="recordCount"></span>)</h4>
        </div>
        <div class="content">
            <table class="mailList">
                <thead>
                <tr>
                    <th style="width: 5%; text-align: center"><input style="zoom: 1.5;" type="checkbox" id="checkAll"></th>
                    <th style="width: 25%">보낸사람</th>
                    <th style="width: 5%"></th>
                    <th style="width: 50%">제목</th>
                    <th style="width: 15%">발송 일자</th>
                </tr>
                </thead>
                <tbody id="mailBody">
                </tbody>
                <tfoot id="buttons"></tfoot>
            </table>
        </div>
        <div class="pageNavi">
        </div>
        <div class="buttonsArea">
            <button type="button" id="deleteBtn">삭제</button>
        </div>
    </div>
</div>
<input type="hidden" id="employeeId" value=${employee.id}>
<script>
    $(document).ready(function() {
        var activeIndex = localStorage.getItem("activeMenuIndex");
        if (activeIndex !== null) {
            $('.detail-menu-item').eq(activeIndex).addClass('active');
        }

        $('.detail-menu-item').on('click', function() {
            $('.detail-menu-item').removeClass('active');
            $(this).addClass('active');
            var index = $('.detail-menu-item').index(this);
            localStorage.setItem("activeMenuIndex", index);
        });

        const $menuBtn = $('.detail-menu-toggle-btn');
        const $detailMenuModal = $('.detail-menu-modal');
        const $closeBtn = $('.detail-modal-close');

        $menuBtn.on('click', function() {
            $detailMenuModal.addClass('active');
            $('body').css('overflow', 'hidden');
        });

        $closeBtn.on('click', function() {
            $detailMenuModal.removeClass('active');
            $('body').css('overflow', '');
        });

        $(window).on('click', function(e) {
            if ($(e.target).is($detailMenuModal)) {
                $detailMenuModal.removeClass('active');
                $('body').css('overflow', '');
            }
        });
        $.ajax({
            url: "/mail/alertList",
            method: "POST",
            data: { employeeId: $("#employeeId").val() }
        }).done(function (resp) {
            $("#importantMailBox").text(resp.importantMailBox);
            $("#allMailBox").text(resp.allMailBox);
            $("#receiveMailBox").text(resp.receiveMailBox);
        });
    });
</script>
<script src="/js/mail/trash.js" type="text/javascript"></script>
<jsp:include page="/WEB-INF/views/template/footer.jsp"/>