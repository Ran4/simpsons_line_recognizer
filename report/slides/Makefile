
all: slides.pdf

slides.pdf: slides.tex
	# pdflatex slides.tex
	pdflatex \\nonstopmode\\input slides.tex

slides.tex: slides.wiki 
	wiki2beamer slides.wiki > slides.tex

clean:
	rm slides.tex slides.pdf
