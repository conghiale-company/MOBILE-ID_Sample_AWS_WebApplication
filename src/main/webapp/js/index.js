let btnModalStartTool = document.getElementById('btnModalStartTool');
let index = document.getElementById('index');
let taxCode = document.getElementById('taxCode');
let config_aws = document.getElementById('config_aws');
let config_send_email = document.getElementById('config_send_email');
let path_file_tax_codes = document.getElementById('path_file_tax_codes');
let modal_body_error = document.getElementById('modal-body-error');
let modal_Error = new bootstrap.Modal(document.getElementById('modalError'));
let modal_Start_Tool = new bootstrap.Modal(document.getElementById('modalStartTool'))


let eventSource = new EventSource("/task")
eventSource.onmessage = function (event) {
    console.log(event.data)
}

btnModalStartTool.addEventListener("click", function() {
    let _index = index.value

    let _taxCode = taxCode.value

    let _config_aws = config_aws.value
    if (!_config_aws || _config_aws === "") {
        modal_body_error.innerText = "Please enter config_aws value to start"
        modal_Error.show()
        return
    }

    let _config_send_email = config_send_email.value
    if (!_config_send_email || _config_send_email === "") {
        modal_body_error.innerText = "Please enter config_send_email value to start"
        modal_Error.show()
        return
    }

    let _path_file_tax_codes = path_file_tax_codes.value
    if (!_path_file_tax_codes || _path_file_tax_codes === "") {
        modal_body_error.innerText = "Please enter path_file_tax_codes value to start"
        modal_Error.show()
        return
    }

    // Make a POST request to start the tool
    let formData = new FormData()
    formData.append("action", 'start')
    formData.append("index", _index)
    formData.append("taxCode", _taxCode)
    formData.append("config_aws", _config_aws)
    formData.append("config_send_email", _config_send_email)
    formData.append("path_file_tax_codes", _path_file_tax_codes)

    fetch('./task', {
        method: 'POST',
        body: formData
    })
        //    .then(response => response.json())
        .then(async data => {
            modal_Start_Tool.hide()
            console.log(data.json())
            modal_body_error.innerText = await data.json()
            modal_Error.show()
        })
        .catch(err => {
            console.error('Error:' + err);
            modal_body_error.innerText = "An error occurred while starting the tool"
            modal_Error.show()
        });

    // Clear input fields
    index.value = ""
    taxCode.value = ""
    config_aws.value = ""
    config_send_email.value = ""
    path_file_tax_codes.value = ""
})

// -1.0-SNAPSHOT