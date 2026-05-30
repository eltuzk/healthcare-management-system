
const http = require('http');

// ================== CONFIGURATION ==================
const CONFIG = {
    host: 'localhost',
    port: 8080,
    appointmentId: 1, 
    token: 'YOUR_RECEPTIONIST_JWT_TOKEN', 
    receiptNumber: 'HD-APT-TEST-001',
    note: 'Thanh toán tiền mặt thủ công tại quầy lễ tân'
};
// ===================================================

const payload = JSON.stringify({
    receiptNumber: CONFIG.receiptNumber,
    note: CONFIG.note
});

const options = {
    hostname: CONFIG.host,
    port: CONFIG.port,
    path: `/api/appointments/${CONFIG.appointmentId}/confirm-payment-manual`,
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${CONFIG.token}`,
        'Content-Length': Buffer.byteLength(payload)
    }
};

console.log(`=== STARTING MANUAL TEST FOR APPOINTMENT ID: ${CONFIG.appointmentId} ===`);
console.log(`Sending request to: POST http://${CONFIG.host}:${CONFIG.port}${options.path}`);
console.log(`Payload: ${payload}\n`);

const req = http.request(options, (res) => {
    let data = '';

    console.log(`Status Code: ${res.statusCode} ${res.statusMessage}`);
    
    res.on('data', (chunk) => {
        data += chunk;
    });

    res.on('end', () => {
        console.log('--- RESPONSE BODY ---');
        try {
            const parsed = JSON.parse(data);
            console.log(JSON.stringify(parsed, null, 2));
            if (res.statusCode === 200) {
                console.log('\n✅ TEST SUCCESS: Manual payment confirmed successfully!');
            } else {
                console.log('\n❌ TEST FAILED: Server returned an error.');
            }
        } catch (e) {
            console.log(data || '(Empty response)');
            console.log('\n❌ TEST FAILED: Response was not valid JSON.');
        }
    });
});

req.on('error', (err) => {
    console.error('Connection Error:', err.message);
    console.log('\n❌ TEST FAILED: Could not connect to local Spring Boot backend. Is it running on port 8080?');
});

req.write(payload);
req.end();
