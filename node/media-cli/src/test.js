const { Configuration, OpenAIApi } = require("openai");

const start = async () => {

    const configuration = new Configuration({
        apiKey: "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",
      });
      const openai = new OpenAIApi(configuration);
      
      try {
        openai.createCompletion({
            model: "text-ada-001",
            prompt: "Create a list of 8 questions for my interview with a science fiction author:\n\n1. What inspired you to become a science fiction author?\n2. How has the genre of science fiction changed since you started writing?\n3. What themes do you explore in your writing?\n4. What advice would you give to aspiring science fiction authors?\n5. What is the most challenging part of writing science fiction?\n6. How has technology impacted the way you write?\n7. What has been your most successful work to date?\n8. What are you currently working on?",
            temperature: 0.5,
            max_tokens: 150,    
            top_p: 1,
            frequency_penalty: 0,
            presence_penalty: 0,
        }).then(response => {
            console.log("Status : " + response.status)
            console.log("Text   : " + response.data.choices[0].text)
   
        })
    } catch (err) {
        console.log(err);
      }


}

start();

