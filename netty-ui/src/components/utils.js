

// npm install uuid
const { v4: uuidv4 } = require('uuid');

function getUuid() {
    return uuidv4()
}


function formatDate(date = new Date()) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

function isEmpty(obj) {
    return obj === null || obj === undefined;
}
function isNotEmpty(obj) {
    return !isEmpty(obj);
}

export {getUuid, formatDate, isEmpty, isNotEmpty}
