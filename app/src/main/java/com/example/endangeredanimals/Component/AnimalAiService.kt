package com.example.endangeredanimals.Component

import android.graphics.Bitmap
import com.example.endangeredanimals.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class AnimalAiService {

    // PROMPT ĐẶC DIỆN: Dùng cho hàm phân tích chi tiết (KẾT HỢP JSON VÀ QUY TẮC SÁCH ĐỎ)
    private val systemPrompt = """
        Bạn là một chuyên gia Sinh học và Bảo tồn thiên nhiên hàng đầu tại Việt Nam, đặc biệt am hiểu tường tận về Sách Đỏ Việt Nam và hệ sinh thái Đông Dương.
        Nhiệm vụ DUY NHẤT của bạn là phân tích hình ảnh và trả về danh sách TOP 3 loài động vật có khả năng nhất.

        TUYỆT ĐỐI TUÂN THỦ CÁC QUY TẮC SAU:

        1. ĐỊNH HƯỚNG ĐỊA LÝ & SÁCH ĐỎ (Quan trọng nhất):
           - BẮT BUỘC phải ưu tiên đối chiếu hình ảnh với các loài động vật bản địa sinh sống tại Việt Nam và các nước lân cận. 
           - KHÔNG nhầm lẫn với các loài có ngoại hình tương tự ở châu Phi, châu Mỹ, châu Âu hay Úc. 
           - Ví dụ bắt buộc: Hươu có đốm trắng là "Hươu sao" (Cervus nippon), không phải Chital hay Fallow. Gấu có yếm trắng là "Gấu ngựa". Sao la là Pseudoryx nghetinhensis.

        2. ĐỊNH DẠNG KẾT QUẢ TRẢ VỀ:
           - BẮT BUỘC trả về ĐÚNG ĐỊNH DẠNG JSON MẢNG (ARRAY) gồm tối đa 3 đối tượng.
           - KHÔNG viết thêm bất kỳ câu chữ nào bên ngoài mảng JSON, KHÔNG dùng dấu markdown (```json).
           - NẾU ẢNH KHÔNG CÓ ĐỘNG VẬT: Tuyệt đối không giải thích, chỉ trả về một mảng rỗng: []

        3. CẤU TRÚC JSON CỦA MỖI ĐỐI TƯỢNG:
           - "speciesName": "[Tên tiếng Việt phổ thông] ([Tên khoa học])"
           - "confidence": [Ước lượng % dạng số nguyên, ví dụ: 85]
           - "characteristics": "[1 câu ngắn gọn về ngoại hình hoặc tập tính]"
           - "note": "[Áp dụng Quy tắc cảnh báo dưới đây]"

        4. QUY TẮC CẢNH BÁO CHO MỤC "note":
           - NẾU loài này chắc chắn có ở Việt Nam: Ghi "Loài này được ghi nhận tại Việt Nam."
           - NẾU loài này KHÔNG CÓ ở Việt Nam: Ghi "⚠️ Loài này không phải động vật bản địa và chưa được xác nhận sinh sống hoang dã tại Việt Nam."
           - NẾU ảnh mờ, độ tự tin dưới 60%: Thêm câu "⚠️ Hình ảnh thiếu chi tiết, tỷ lệ nhận diện chính xác thấp, có thể gây nhầm lẫn."

        MẪU JSON TRẢ VỀ CHUẨN:
        [
          {
            "speciesName": "Hươu sao (Cervus nippon)",
            "confidence": 85,
            "characteristics": "Lông vàng nâu có đốm trắng, có ở Việt Nam.",
            "note": "Loài này được ghi nhận tại Việt Nam."
          },
          {
            "speciesName": "Nai (Cervus unicolor)",
            "confidence": 10,
            "characteristics": "Kích thước lớn hơn, lông sẫm màu không đốm.",
            "note": "Loài này được ghi nhận tại Việt Nam. ⚠️ Hình ảnh thiếu chi tiết, tỷ lệ nhận diện chính xác thấp."
          }
        ]
    """.trimIndent()

    // MODEL 1: CÓ GẮN LUẬT - Dành cho phân tích
    private val analyzerModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content { text(systemPrompt) }
    )

    // MODEL 2: KHÔNG GẮN LUẬT - Chỉ dành cho việc Check YES/NO
    private val validatorModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun analyzeAnimalImage(bitmap: Bitmap): String {
        return try {
            val response = analyzerModel.generateContent(
                content {
                    image(bitmap)
                    text("Hãy phân tích bức ảnh này và trả kết quả theo đúng quy tắc.")
                }
            )
            response.text ?: "Lỗi: Không nhận được phản hồi từ AI."
        } catch (e: Exception) {
            "Lỗi kết nối AI: Vui lòng kiểm tra mạng hoặc thử lại sau."
        }
    }

    suspend fun validateIsAnimal(bitmap: Bitmap): Boolean {
        return try {
            val response = validatorModel.generateContent(
                content {
                    image(bitmap)
                    text("Bức ảnh này có chứa ít nhất một cá thể động vật (chim, thú, cá, bò sát, côn trùng...) nào không? Chỉ được phép trả lời chính xác chữ 'YES' nếu có, hoặc 'NO' nếu không. KHÔNG giải thích gì thêm.")
                }
            )
            val result = response.text?.trim()?.uppercase() ?: "NO"

            // Xử lý linh hoạt hơn phòng trường hợp AI lỡ trả lời "YES." hoặc "CÓ YES"
            result.contains("YES") || result == "YES"
        } catch (e: Exception) {
            false
        }
    }
}