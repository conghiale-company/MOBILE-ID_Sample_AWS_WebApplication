let btnModalStartTool = document.getElementById('btnModalStartTool');
let index = document.getElementById('index');
let taxCode = document.getElementById('taxCode');
let config_aws = document.getElementById('config_aws');
let config_send_email = document.getElementById('config_send_email');
let path_file_tax_codes = document.getElementById('path_file_tax_codes');
let modal_body_error = document.getElementById('modal-body-error');
let modal_Error = new bootstrap.Modal(document.getElementById('modalError'));
let modal_Start_Tool = new bootstrap.Modal(document.getElementById('modalStartTool'))

let isStatus = true

// bien luu tru gia tri server phan hoi [START]
let state = {
    _isRunning: false,
    _status: null,
    _index: -1,
    _taxCode: null,
    _config_aws: null,
    _configSendEmail: null,
    _pathFileTaxCodes: null,
    _startDay: null,
    _endDay: null,
    _previousTaxCode: null,
    _previousStatus: null,
    _currentTaxCode: null,
    _currentStatus: null,
    _message: null,
    _numberNotFound: 0,
    _numberParameterIsInvalid: 0,
    _numberUnknownException: 0,
    _numberResponseIsNull: 0,
    _numberCaptchaInvalid: 0,
    _numberErrors: 0,
    _numberSuccessfully: 0,
    _numberTaxCode: 0,
    _previousIndex: -1,
    _currentIndex: -1
};

// bien luu tru gia tri server phan hoi [END]

// let eventSource = new EventSource("/task")
// eventSource.onmessage = function (event) {
//     console.log(event.data)
// }

// Create a proxy to monitor changes and update the UI
let stateProxy = new Proxy(state, {
    set(target, property, value) {
        target[property] = value;

        // console.log("[Proxy] property... " + property)
        // Update UI based on property name and value
        updateUI(property, value);

        // Return true to indicate the assignment was successful
        return true;
    }
});

// Khoi tao web
let status__tool = document.querySelector('.status__tool')
let statusTool = document.getElementById('statusTool');

if (isStatus === true) {
    if (stateProxy._isRunning === true) {
        status__tool.style.border = "2px dashed #25A31A"
        statusTool.style.color = '#25A31A'
        statusTool.innerText = 'RUNNING';
    } else if (stateProxy._isRunning === false) {
        status__tool.style.border = "2px dashed #DF3838"
        statusTool.style.color = '#DF3838'
        statusTool.innerText = 'STOPPED'
    }
} else if (isStatus === false) {
    status__tool.style.border = "2px dashed #DF3838"
    statusTool.style.color = '#DF3838'
    statusTool.innerText = 'STOPPED'
}


// Kết nối tới WebSocket\
// console.log("window.location.hostname: " + window.location.hostname)
// const socket = new WebSocket(`wss://${window.location.hostname}:443/Sample_AWS_WebApplication-1.0-SNAPSHOT/socket-handle-tax-code`);
// const socket = new WebSocket(`ws://${window.location.hostname}:8080/Sample_AWS_WebApplication-1.0-SNAPSHOT/socket-handle-tax-code`);
let socket;
if (window.location.hostname === "localhost") {
    socket = new WebSocket(`ws://${window.location.hostname}:8080/Sample_AWS_WebApplication-1.0-SNAPSHOT/socket-handle-tax-code`);
} else {
    socket = new WebSocket(`wss://${window.location.hostname}:443/Sample_AWS_WebApplication-1.0-SNAPSHOT/socket-handle-tax-code`);
}


if (window.location.hostname === "taxclient01.mobile-id.vn") {
    stateProxy._config_aws = "/root/tax-info-client-data/config_aws_dev.cfg"
    stateProxy._configSendEmail = "/root/tax-info-client-data/config_send_email.cfg"
    stateProxy._pathFileTaxCodes = "/root/tax-info-client-data/Tax_Code_500k.txt"
} else if (window.location.hostname === "taxclient02.mobile-id.vn") {
    stateProxy._config_aws = "/root/tax-info-client-data/config_aws_isapp.cfg"
    stateProxy._configSendEmail = "/root/tax-info-client-data/config_send_email.cfg"
    stateProxy._pathFileTaxCodes = "/root/tax-info-client-data/Tax_Code_499k.txt"
} else if (window.location.hostname === "localhost") {
    stateProxy._config_aws = "D:\\Data\\MOBILE_ID\\DTIS-V2-Utility\\Test_Tool_AWS\\config_aws_dev.cfg"
    stateProxy._configSendEmail = "D:\\Data\\MOBILE_ID\\DTIS-V2-Utility\\Test_Tool_AWS\\config_send_email.cfg"
    stateProxy._pathFileTaxCodes = "D:\\Data\\MOBILE_ID\\DTIS-V2-Utility\\Test_Tool_AWS\\Tax_Code_Test.txt"
}

// onMessage
// - Ham nay duoc goi khi client nhan duoc tin nhan tu server
// - Ham nay thuong duoc su dung de nhan phan hoi hoac thong bao tu server
socket.onmessage = (event) => {
    let message = JSON.parse(event.data)
    // console.log("[RECEIVED] CLIENT RECEIVED SERVER RESPONSE: \n" + JSON.stringify(message))

    // if (message.status) {
    //     let status = message.status

    //     if (status.includes("[ERROR]")) {
    //         modal_body_error.innerText = status.replace("[ERROR] ", "")
    //         modal_Error.show()
    //     } else {  
    //         updateUI_StatusTool(status)
    //     }
    // }

    if (message.isRunning) {
        stateProxy._isRunning = message.isRunning
    }

    if (message.status) {
        if (message.status.includes("[ERROR]")) {
            document.getElementById("modalErrorLabel").innerText = message.status.replace("[ERROR] ", "")
            document.getElementById("modal-body-error").innerText = message.message.
            modal_Error.show()
        }

        stateProxy._status = (message.status === null || message.status === "") ? null : message.status
    }

    if (message.taxCode) {
        stateProxy._taxCode = (message.taxCode === "") ? null : message.taxCode
    }

    if (message.index) {
        stateProxy._index = (message.index === "") ? null : message.index
    }

    if (message.config_aws) {
        stateProxy._config_aws = (message.PATH_AWS_CONFIG === "") ? null : message.PATH_AWS_CONFIG
    }

    if (message.configSendEmail) {
        stateProxy._configSendEmail = (message.PATH_SEND_EMAIL_CONFIG === "") ? null : message.PATH_SEND_EMAIL_CONFIG
    }

    if (message.pathFileTaxCodes) {
        stateProxy._pathFileTaxCodes = (message.PATH === "") ? null : message.PATH
    }

    if (message.startDay) {
        stateProxy._startDay = (message.startDay === "")? null : message.startDay
    }

    if (message.endDay) {
        stateProxy._endDay = (message.endDay === "")? null : message.endDay
    }

    if (message.previousTaxCode) {
        stateProxy._previousTaxCode = message.previousTaxCode
    }

    if (message.previousStatus) {
        stateProxy._previousStatus = message.previousStatus
    }

    if (message.currentTaxCode) {
        stateProxy._currentTaxCode = message.currentTaxCode
    }

    if (message.currentStatus) {
        stateProxy._currentStatus = message.currentStatus
    }

    if (message.message) {
        stateProxy._message = message.message
    }

    if (message.numberNotFound) {
        stateProxy._numberNotFound = message.numberNotFound
    }

    if (message.numberParameterIsInvalid) {
        stateProxy._numberParameterIsInvalid = message.numberParameterIsInvalid
    }

    if (message.numberUnknownException) {
        stateProxy._numberUnknownException = message.numberUnknownException
    }

    if (message.numberResponseIsNull) {
        stateProxy._numberResponseIsNull = message.numberResponseIsNull
    }

    if (message.numberCaptchaInvalid) {
        stateProxy._numberCaptchaInvalid = message.numberCaptchaInvalid
    }

    if (message.numberErrors) {
        stateProxy._numberErrors = message.numberErrors
    }

    if (message.numberSuccessfully) {
        stateProxy._numberSuccessfully = message.numberSuccessfully
    }

    if (message.numberTaxCode) {
        stateProxy._numberTaxCode = message.numberTaxCode
    }

    if (message.previousIndex) {
        stateProxy._previousIndex = message.previousIndex
    }

    if (message.currentIndex) {
        stateProxy._currentIndex = message.currentIndex
    }

    // console.log("[CHECK]" + ("RespPayload" in message) + " - " + (message.RespPayload === "object"))
    if ("RespPayload" in message) {
        // console.log("\n\n[RespPayload] " + JSON.stringify(message.RespPayload))
        let RespPayload = message.RespPayload

        if ("isRunning" in RespPayload) {
            stateProxy._isRunning = RespPayload.isRunning
        } 

        if ("taxCode" in RespPayload) {
            stateProxy._taxCode = RespPayload.taxCode
        }

        if ("config_aws" in RespPayload) {
            stateProxy._config_aws = RespPayload.config_aws
        }

        if ("configSendEmail" in RespPayload) {
            stateProxy._configSendEmail = RespPayload.configSendEmail
        }

        if ("pathFileTaxCodes" in RespPayload) {
            stateProxy._pathFileTaxCodes = RespPayload.pathFileTaxCodes
        }

        if ("startDay" in RespPayload) {
            stateProxy._startDay = RespPayload.startDay
        }

        if ("endDay" in RespPayload) {
            stateProxy._endDay = RespPayload.endDay
        }

        if ("previousTaxCode" in RespPayload) {
            stateProxy._previousTaxCode = RespPayload.previousTaxCode
        }

        if ("previousStatus" in RespPayload) {
            stateProxy._previousStatus = RespPayload.previousStatus
        }

        if ("currentTaxCode" in RespPayload) {
            stateProxy._currentTaxCode = RespPayload.currentTaxCode
        }

        if ("currentStatus" in RespPayload) {
            stateProxy._currentStatus = RespPayload.currentStatus
        }

        if ("message" in RespPayload) {
            stateProxy._message = RespPayload.message
        }

        if ("index" in RespPayload) {
            stateProxy._index = RespPayload.index
        }

        if ("numberNotFound" in RespPayload) {
            stateProxy._numberNotFound = RespPayload.numberNotFound
        }

        if ("numberParameterIsInvalid" in RespPayload) {
            stateProxy._numberParameterIsInvalid = RespPayload.numberParameterIsInvalid
        }

        if ("numberUnknownException" in RespPayload) {
            stateProxy._numberUnknownException = RespPayload.numberUnknownException
        }

        if ("numberResponseIsNull" in RespPayload) {
            stateProxy._numberResponseIsNull = RespPayload.numberResponseIsNull
        }

        if ("numberCaptchaInvalid" in RespPayload) {
            stateProxy._numberCaptchaInvalid = RespPayload.numberCaptchaInvalid
        }

        if ("numberErrors" in RespPayload) {
            stateProxy._numberErrors = RespPayload.numberErrors
        }

        if ("numberSuccessfully" in RespPayload) {
            stateProxy._numberSuccessfully = RespPayload.numberSuccessfully
        }

        if ("numberTaxCode" in RespPayload) {
            stateProxy._numberTaxCode = RespPayload.numberTaxCode
        }

        if ("previousIndex" in RespPayload) {
            stateProxy._previousIndex = RespPayload.previousIndex
        }

        if ("currentIndex" in RespPayload) {
            stateProxy._currentIndex = RespPayload.currentIndex
        }
    }
}

// onOpen
// - Ham nay duoc goi khi ket noi WebSocket da mo thanh cong
// - Day la thoi diem da thiet lap ket noi voi server va co the bat dau gui hoac nhan du lieu
socket.onopen = (event) => {
    console.log("[CLIENT OPEN] CONNECTED TO WEBSOCKET SERVER." + event.timeStamp);
}

// onClose
// - Ham nay duoc goi khi ket noi WebSocket dong
// - Thuoc tinh event.wasClean cho biet ket noi co duoc dong mot cach binh thuong hay khong
// - Cac thong tin event.code và cent.reason chua ma loi va ly do dong (neu co)
// - Day la luc thich hop de cap nhat UI, bao cao cho nguoi dung biet ket noi da bi mat và chuan bi cac thao tac tai ket
// noi (neu can)
socket.onclose = (event) => {
    if (event.wasClean) {
        console.log("[CLIENT CLOSE] WEBSOCKET CONNECTION CLOSED CLEANLY.");
    } else {
        console.error("[CLIENT CLOSE] [ERROR] WEBSOCKET CONNECTION CLOSED UNEXPECTEDLY.");
    }
    console.log("[CLIENT CLOSE] Code:", event.code, "Reason:", event.reason);
}

// onError
// - Ham nay duoc goi khi co loi xay ra trong ket noi WebSocket
// - Vi du ve loi co the la khong ket noi den server hoac loi ket noi mang
// - Ham nay thuong duoc su dung de ghi log loi va co the thuc hien mot so thao tac khac phuc, chang han
// nhu thong bao loi cho nguoi dung hoac co gang ket noi lai
socket.onerror = function(error) {
    console.error("[CLIENT ERROR] WEBSOCKET ERROR:", error);
};

btnModalStartTool.addEventListener("click", function() {
    let __index = index.value
    let __taxCode = taxCode.value

    let __config_aws = config_aws.value
    if (!__config_aws || __config_aws === "") {
        modal_body_error.innerText = "Please enter config_aws value to start"
        modal_Error.show()
        return
    }

    let __config_send_email = config_send_email.value
    if (!__config_send_email || __config_send_email === "") {
        modal_body_error.innerText = "Please enter config_send_email value to start"
        modal_Error.show()
        return
    }

    let __path_file_tax_codes = path_file_tax_codes.value
    if (!__path_file_tax_codes || __path_file_tax_codes === "") {
        modal_body_error.innerText = "Please enter path_file_tax_codes value to start"
        modal_Error.show()
        return
    }

    // Tạo đối tượng JSON chứa các dữ liệu
    let data = {
        action: "start",
        index: __index,
        taxCode: __taxCode,
        config_aws: __config_aws,
        config_send_email: __config_send_email,
        path_file_tax_codes: __path_file_tax_codes
    };

    socket.send(JSON.stringify(data))

    // Ẩn modal
    document.getElementById("modalStartTool").classList.remove("show");
    document.getElementById("modalStartTool").style.display = "none";

    // Xóa tất cả các phần tử có class "modal-backdrop"
    document.querySelectorAll(".modal-backdrop").forEach(function(backdrop) {
        backdrop.remove();
    });

    isStatus = true;

    // Cap nhat gia tri server phan hoi
    stateProxy._index = __index
    stateProxy._taxCode = __taxCode
    stateProxy._config_aws = __config_aws
    stateProxy._configSendEmail = __config_send_email
    stateProxy._pathFileTaxCodes = __path_file_tax_codes

    // Clear input fields
    index.value = ""
    taxCode.value = ""
    // config_aws.value = ""
    // config_send_email.value = ""
    // path_file_tax_codes.value = ""
})

document.getElementById("btnStop").addEventListener("click", function() { 
    isStatus = false
    let data = {
        action: "stop"
    }

    socket.send(JSON.stringify(data))
})

// Function to update the UI
function updateUI(property, value) {
    if (property === "_isRunning") {
        updateUI_isRunning(value)
    }

    if (property === "_status") {
        updateUI_StatusTool(value)
    }

    if (property === "_index") {
        updateUI_Index(value)
    }

    if (property === "_taxCode") {
        updateUI_TaxCode(value)
    }

    if (property === "_config_aws") {
        updateUI_ConfigAWS(value)
    }

    if (property === "_configSendEmail") {
        updateUI_ConfigSendEmail(value)
    }

    if (property === "_pathFileTaxCodes") {
        updateUI_PathFileTaxCodes(value)
    }

    if (property === "_startDay") {
        updateUI_StartDay(value)
    }

    if (property === "_endDay") {
        updateUI_EndDay(value)
    }

    if (property === "_previousTaxCode") {
        updateUI_PreviousTaxCode(value)
    }

    if (property === "_previousStatus") {
        updateUI_PreviousStatus(value)
    }

    if (property === "_currentTaxCode") {
        updateUI_CurrentTaxCode(value)
    }

    if (property === "_currentStatus") {
        updateUI_CurrentStatus(value)
    }

    if (property === "_message") {
        updateUI_Message(value)
    }

    if (property === "_numberNotFound") {
        updateUI_NumberNotFound(value)
    }

    if (property === "_numberParameterIsInvalid") {
        updateUI_NumberParameterIsInvalid(value)
    }

    if (property === "_numberUnknownException") {
        updateUI_NumberUnknownException(value)
    }

    if (property === "_numberResponseIsNull") {
        updateUI_NumberResponseIsNull(value)
    }

    if (property === "_numberCaptchaInvalid") {
        updateUI_NumberCaptchaInvalid(value)
    }

    if (property === "_numberErrors") {
        updateUI_NumberErrors(value)
    }

    if (property === "_numberSuccessfully") {
        updateUI_NumberSuccessfully(value)
    }

    if (property === "_numberSuccessfully") {
        updateUI_NumberSuccessfully(value)
    }

    if (property === "_numberTaxCode") {
        updateUI_NumberTaxCode(value)
    }

    if (property === "_previousIndex") {
        updateUI_PreviousIndex(value)
    }

    if (property === "_currentIndex") {
        updateUI_CurrentIndex(value)
    }

    const element = document.getElementById(property); // Assuming each element has an id matching the property name
    if (element) {
        element.textContent = value; // Update element content with the new value
    }
}

function updateUI_StatusTool(status) {
    let status__tool = document.querySelector('.status__tool')
    let statusTool = document.getElementById('statusTool');

    if (isStatus === true) {
        if (status.includes("LOGIN_SUCCESS") || status.includes("SUCCESS")) {
            stateProxy._isRunning = true
            status__tool.style.border = "2px dashed #25A31A"
            statusTool.style.color = '#25A31A'
            statusTool.innerText = 'RUNNING';
        } else if (status.includes("[STOP]") || status.includes("[ERROR]")) {
            stateProxy._isRunning = false
            status__tool.style.border = "2px dashed #DF3838"
            statusTool.style.color = '#DF3838'
            statusTool.innerText = 'STOPPED'
        }
    } else if (isStatus === false) {
        stateProxy._isRunning = false
        status__tool.style.border = "2px dashed #DF3838"
        statusTool.style.color = '#DF3838'
        statusTool.innerText = 'STOPPED'
    }

}

function updateUI_isRunning(_isRunning) {
    // console.log("isStatus: " + isStatus)
    // console.log("_isRunning: " + _isRunning)
    if (isStatus === true) {
        if (_isRunning === true) {
            status__tool.style.border = "2px dashed #25A31A"
            statusTool.style.color = '#25A31A'
            statusTool.innerText = 'RUNNING';
        } else if (_isRunning === false) {
            status__tool.style.border = "2px dashed #DF3838"
            statusTool.style.color = '#DF3838'
            statusTool.innerText = 'STOPPED'
        }
    } else if (isStatus === false) {
        status__tool.style.border = "2px dashed #DF3838"
        statusTool.style.color = '#DF3838'
        statusTool.innerText = 'STOPPED'
    }

}

function updateUI_Index(_index) {
    // Update
}

function updateUI_TaxCode(_taxCode) {
    // Update
}

function updateUI_ConfigAWS(_config_aws) {
    // Update
    config_aws.value = _config_aws
}

function updateUI_ConfigSendEmail(_configSendEmail) {
    // Update
    config_send_email.value = _configSendEmail
}

function updateUI_PathFileTaxCodes(_pathFileTaxCodes) {
    // Update
    path_file_tax_codes.value = _pathFileTaxCodes
}

function updateUI_StartDay(_startDay) {
    // Update
    document.getElementById("startDay").value = _startDay
}

function updateUI_EndDay(_endDay) {
    // Update
    document.getElementById("endDay").value = _endDay
}

function updateUI_PreviousTaxCode(_previousTaxCode) {
    // Update
    document.getElementById("previousTaxCode").innerText = _previousTaxCode
}

function updateUI_PreviousStatus(_previousStatus) {
    // Update
    document.getElementById("previousStatus").innerText = _previousStatus

    if (_previousStatus === "SUCCESS") {
        document.getElementById("previousStatus").style.backgroundColor = "#25A31A"
    } else {
        document.getElementById("previousStatus").style.backgroundColor = "#DF3838"
    }
}

function updateUI_CurrentTaxCode(_currentTaxCode) {
    // Update
    document.getElementById("currentTaxCode").innerText = _currentTaxCode
}

function updateUI_CurrentStatus(_currentStatus) {
    // Update
    document.getElementById("currentStatus").innerText = _currentStatus

    if (_currentStatus === "SUCCESS") {
        document.getElementById("currentStatus").style.backgroundColor = "#25A31A"
    } else if (_currentStatus === "PROCESSING..."){
        document.getElementById("currentStatus").style.backgroundColor = "#0abde3"
    } else {
        document.getElementById("currentStatus").style.backgroundColor = "#DF3838"
    }
}

function updateUI_Message(_message) {
    // Update
}

function updateUI_NumberNotFound(_numberNotFound) {
    // Update
    document.getElementById("numberNotFound").innerText = _numberNotFound
}

function updateUI_NumberParameterIsInvalid(_numberParameterIsInvalid) {
    // Update
    document.getElementById("numberParameterIsInvalid").innerText = _numberParameterIsInvalid
}

function updateUI_NumberUnknownException(_numberUnknownException) {
    // Update
    document.getElementById("numberUnknownException").innerText = _numberUnknownException
}

function updateUI_NumberResponseIsNull(_numberResponseIsNull) {
    // Update
    document.getElementById("numberResponseIsNull").innerText = _numberResponseIsNull
}

function updateUI_NumberCaptchaInvalid(_numberCaptchaInvalid) {
    // Update
    document.getElementById("numberCaptchaInvalid").innerText = _numberCaptchaInvalid
}

function updateUI_NumberErrors(_numberErrors) {
    // Update
    document.getElementById("numberErrors").innerText = _numberErrors
}

function updateUI_NumberSuccessfully(_numberSuccessfully) {
    // Update
    document.getElementById("numberSuccessfully").innerText = _numberSuccessfully
}

function updateUI_NumberTaxCode(_numberTaxCode) {
    // update
    document.getElementById("numberTaxCode").innerText = _numberTaxCode
}

function updateUI_PreviousIndex(_previousIndex) {
    // Update
    document.getElementById("previousIndex").innerText = _previousIndex
}

function updateUI_CurrentIndex(_currentIndex) {
    // Update
    document.getElementById("currentIndex").innerText = _currentIndex
}